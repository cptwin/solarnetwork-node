/* ==================================================================
 * DefaultKeystoreService.java - Dec 5, 2012 9:10:53 AM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.setup.impl;

import static net.solarnetwork.node.SetupSettings.SETUP_TYPE_KEY;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import net.solarnetwork.node.SSLService;
import net.solarnetwork.node.backup.BackupResource;
import net.solarnetwork.node.backup.BackupResourceProvider;
import net.solarnetwork.node.backup.ResourceBackupResource;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.setup.PKIService;
import net.solarnetwork.support.CertificateException;
import net.solarnetwork.support.CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.FileCopyUtils;

/**
 * Service for managing a {@link KeyStore}.
 * 
 * <p>
 * This implementation maintains a key store with two primary aliases:
 * {@code ca} and {@code node}. The key store is created as needed, and a random
 * password is generated and assigned to the key store. The password is stored
 * in the Settings database, using the {@link #KEY_PASSWORD} key. This key store
 * is then used to implement {@link SSLService} and is used as both the key and
 * trust store for SSL connections returned by that API.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class DefaultKeystoreService implements PKIService, SSLService, BackupResourceProvider {

	private static final String BACKUP_RESOURCE_NAME_KEYSTORE = "node.jks";

	/** The default value for the {@code keyStorePath} property. */
	public static final String DEFAULT_KEY_STORE_PATH = "conf/tls/node.jks";

	/** The settings key for the key store password. */
	public static final String KEY_PASSWORD = "solarnode.keystore.pw";

	private static final int PASSWORD_LENGTH = 20;

	private String keyStorePath = DEFAULT_KEY_STORE_PATH;
	private String nodeAlias = "node";
	private String caAlias = "ca";
	private int keySize = 2048;
	private String manualKeyStorePassword;

	@Resource
	private CertificateService certificateService;

	@Resource
	private SettingDao settingDao;

	private SSLSocketFactory solarInSocketFactory;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public String getKey() {
		return DefaultKeystoreService.class.getName();
	}

	@Override
	public Iterable<BackupResource> getBackupResources() {
		File ksFile = new File(keyStorePath);
		if ( !(ksFile.isFile() && ksFile.canRead()) ) {
			return Collections.emptyList();
		}
		List<BackupResource> result = new ArrayList<BackupResource>(1);
		result.add(new ResourceBackupResource(new FileSystemResource(ksFile),
				BACKUP_RESOURCE_NAME_KEYSTORE));
		return result;
	}

	@Override
	public boolean restoreBackupResource(BackupResource resource) {
		if ( resource != null
				&& BACKUP_RESOURCE_NAME_KEYSTORE.equalsIgnoreCase(resource.getBackupPath()) ) {
			final File ksFile = new File(keyStorePath);
			final File ksDir = ksFile.getParentFile();
			if ( !ksDir.isDirectory() ) {
				if ( !ksDir.mkdirs() ) {
					log.warn("Error creating keystore directory {}", ksDir.getAbsolutePath());
					return false;
				}
			}
			synchronized ( this ) {
				try {
					FileCopyUtils.copy(resource.getInputStream(), new FileOutputStream(ksFile));
					ksFile.setLastModified(resource.getModificationDate());
					return true;
				} catch ( IOException e ) {
					log.error("IO error restoring keystore resource {}: {}", ksFile.getAbsolutePath(),
							e.getMessage());
					return false;
				}
			}
		}
		return false;
	}

	private String getKeyStorePassword() {
		if ( manualKeyStorePassword != null && manualKeyStorePassword.length() > 0 ) {
			return manualKeyStorePassword;
		}
		String result = getSetting(KEY_PASSWORD);
		if ( result == null ) {
			// generate new random password
			try {
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				final int start = 32;
				final int end = 126;
				final int range = end - start;
				char[] passwd = new char[PASSWORD_LENGTH];
				for ( int i = 0; i < PASSWORD_LENGTH; i++ ) {
					passwd[i] = (char) (random.nextInt(range) + start);
				}
				result = new String(passwd);
				saveSetting(KEY_PASSWORD, result);
			} catch ( NoSuchAlgorithmException e ) {
				throw new CertificateException("Error creating random key store password", e);
			}
		}
		return result;
	}

	@Override
	public boolean isNodeCertificateValid(String issuerDN) throws CertificateException {
		KeyStore keyStore = loadKeyStore();
		X509Certificate x509 = null;
		try {
			if ( keyStore == null || !keyStore.containsAlias(nodeAlias) ) {
				return false;
			}
			Certificate cert = keyStore.getCertificate(nodeAlias);
			if ( !(cert instanceof X509Certificate) ) {
				return false;
			}
			x509 = (X509Certificate) cert;
			x509.checkValidity();
			X500Principal issuer = new X500Principal(issuerDN);
			if ( !x509.getIssuerX500Principal().equals(issuer) ) {
				log.debug("Certificate issuer {} not same as expected {}", x509.getIssuerX500Principal()
						.getName(), issuer.getName());
				return false;
			}
			return true;
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error checking for node certificate", e);
		} catch ( CertificateExpiredException e ) {
			log.debug("Certificate {} has expired", x509.getSubjectDN().getName());
		} catch ( CertificateNotYetValidException e ) {
			log.debug("Certificate {} not valid yet", x509.getSubjectDN().getName());
		}
		return false;
	}

	@Override
	public X509Certificate generateNodeSelfSignedCertificate(String dn) throws CertificateException {
		KeyStore keyStore = loadKeyStore();
		return createSelfSignedCertificate(keyStore, dn, nodeAlias);
	}

	private X509Certificate createSelfSignedCertificate(KeyStore keyStore, String dn, String alias) {
		try {
			// create new key pair for the node
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(keySize, new SecureRandom());
			KeyPair keypair = keyGen.generateKeyPair();
			PublicKey publicKey = keypair.getPublic();
			PrivateKey privateKey = keypair.getPrivate();

			Certificate cert = certificateService.generateCertificate(dn, publicKey, privateKey);
			keyStore.setKeyEntry(alias, privateKey, getKeyStorePassword().toCharArray(),
					new Certificate[] { cert });
			saveKeyStore(keyStore);
			return (X509Certificate) cert;
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error setting up node key pair", e);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error setting up node key pair", e);
		}
	}

	private void saveTrustedCertificate(X509Certificate cert, String alias) {
		KeyStore keyStore = loadKeyStore();
		try {
			log.info("Installing trusted CA certificate {}", cert.getSubjectDN());
			keyStore.setCertificateEntry(alias, cert);
			saveKeyStore(keyStore);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error saving trusted certificate", e);
		}
	}

	@Override
	public void saveCACertificate(X509Certificate cert) throws CertificateException {
		saveTrustedCertificate(cert, caAlias);
	}

	@Override
	public String generateNodePKCS10CertificateRequestString() throws CertificateException {
		KeyStore keyStore = loadKeyStore();
		Key key;
		try {
			key = keyStore.getKey(nodeAlias, getKeyStorePassword().toCharArray());
		} catch ( UnrecoverableKeyException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error opening node private key", e);
		}
		assert key instanceof PrivateKey;
		Certificate cert;
		try {
			cert = keyStore.getCertificate(nodeAlias);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node certificate", e);
		}
		assert cert instanceof X509Certificate;
		return certificateService.generatePKCS10CertificateRequestString((X509Certificate) cert,
				(PrivateKey) key);
	}

	@Override
	public String generateNodePKCS7CertificateString() throws CertificateException {
		KeyStore keyStore = loadKeyStore();
		Key key;
		try {
			key = keyStore.getKey(nodeAlias, getKeyStorePassword().toCharArray());
		} catch ( UnrecoverableKeyException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error opening node private key", e);
		}
		assert key instanceof PrivateKey;
		Certificate cert;
		try {
			cert = keyStore.getCertificate(nodeAlias);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node certificate", e);
		}
		assert cert instanceof X509Certificate;
		return certificateService
				.generatePKCS7CertificateChainString(new X509Certificate[] { (X509Certificate) cert });
	}

	@Override
	public String generateNodePKCS7CertificateChainString() throws CertificateException {
		KeyStore keyStore = loadKeyStore();
		Key key;
		try {
			key = keyStore.getKey(nodeAlias, getKeyStorePassword().toCharArray());
		} catch ( UnrecoverableKeyException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error opening node private key", e);
		}
		assert key instanceof PrivateKey;
		Certificate[] chain;
		try {
			chain = keyStore.getCertificateChain(nodeAlias);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node certificate", e);
		}
		X509Certificate[] x509Chain = new X509Certificate[chain.length];
		for ( int i = 0; i < chain.length; i++ ) {
			assert chain[i] instanceof X509Certificate;
			x509Chain[i] = (X509Certificate) chain[i];
		}
		return certificateService.generatePKCS7CertificateChainString(x509Chain);
	}

	@Override
	public X509Certificate getNodeCertificate() throws CertificateException {
		return getNodeCertificate(loadKeyStore());
	}

	private X509Certificate getNodeCertificate(KeyStore keyStore) {
		X509Certificate nodeCert;
		try {
			nodeCert = (X509Certificate) keyStore.getCertificate(nodeAlias);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node certificate", e);
		}
		return nodeCert;
	}

	@Override
	public X509Certificate getCACertificate() throws CertificateException {
		return getCACertificate(loadKeyStore());
	}

	private X509Certificate getCACertificate(KeyStore keyStore) {
		X509Certificate nodeCert;
		try {
			nodeCert = (X509Certificate) keyStore.getCertificate(caAlias);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node certificate", e);
		}
		return nodeCert;
	}

	@Override
	public void saveNodeSignedCertificate(String pem) throws CertificateException {
		KeyStore keyStore = loadKeyStore();
		Key key;
		try {
			key = keyStore.getKey(nodeAlias, getKeyStorePassword().toCharArray());
		} catch ( UnrecoverableKeyException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node private key", e);
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error opening node private key", e);
		}
		X509Certificate nodeCert = getNodeCertificate(keyStore);
		X509Certificate caCert = getCACertificate(keyStore);

		X509Certificate[] chain = certificateService.parsePKCS7CertificateChainString(pem);

		if ( chain.length < 1 ) {
			throw new CertificateException("No certificates avaialble");
		}

		if ( chain.length > 1 ) {
			// we have to trust the parents... the end of the chain must be our CA
			try {
				final int caIdx = chain.length - 1;
				if ( caCert == null ) {
					// if we don't have a CA cert yet, install that now
					log.info("Installing trusted CA certificate {}", chain[caIdx].getSubjectDN());
					keyStore.setCertificateEntry(caAlias, chain[caIdx]);
					caCert = chain[caIdx];
				} else {
					// verify CA is the same... maybe we shouldn't do this?
					if ( !chain[caIdx].getSubjectDN().equals(caCert.getSubjectDN()) ) {
						throw new CertificateException("Chain CA "
								+ chain[caIdx].getSubjectDN().getName() + " does not match expected "
								+ caCert.getSubjectDN().getName());
					}
					if ( !chain[caIdx].getIssuerDN().equals(caCert.getIssuerDN()) ) {
						throw new CertificateException("Chain CA "
								+ chain[caIdx].getIssuerDN().getName() + " does not match expected "
								+ caCert.getIssuerDN().getName());
					}
				}
				// install intermediate certs...
				for ( int i = caIdx - 1, j = 1; i > 0; i--, j++ ) {
					String alias = caAlias + "sub" + j;
					log.info("Installing trusted intermediate certificate {}", chain[i].getSubjectDN());
					keyStore.setCertificateEntry(alias, chain[i]);
				}
			} catch ( KeyStoreException e ) {
				throw new CertificateException("Error storing CA chain", e);
			}
		} else {
			// put CA at end of chain
			if ( caCert == null ) {
				throw new CertificateException("No CA certificate available");
			}
			chain = new X509Certificate[] { chain[0], caCert };
		}

		// the issuer must be our CA cert subject...
		if ( !chain[0].getIssuerDN().equals(chain[1].getSubjectDN()) ) {
			throw new CertificateException("Issuer " + chain[0].getIssuerDN().getName()
					+ " does not match expected " + chain[1].getSubjectDN().getName());
		}

		// the subject must be our node's existing subject...
		if ( !chain[0].getSubjectDN().equals(nodeCert.getSubjectDN()) ) {
			throw new CertificateException("Subject " + chain[0].getIssuerDN().getName()
					+ " does not match expected " + nodeCert.getSubjectDN().getName());
		}

		log.info("Installing node certificate reply {} issued by {}", chain[0].getSubjectDN().getName(),
				chain[0].getIssuerDN().getName());
		try {
			keyStore.setKeyEntry(nodeAlias, key, getKeyStorePassword().toCharArray(), chain);
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error opening node certificate", e);
		}

		saveKeyStore(keyStore);
	}

	private synchronized void resetFromKeyStoreChange() {
		solarInSocketFactory = null;
	}

	@Override
	public synchronized SSLSocketFactory getSolarInSocketFactory() {
		if ( solarInSocketFactory == null ) {
			File ksFile = new File(keyStorePath);
			if ( !ksFile.isFile() ) {
				log.debug("Using default SSLSocketFactory as no keystore exists.");
				return (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
			try {
				KeyStore keyStore = loadKeyStore();
				TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
				trustManagerFactory.init(keyStore);

				X509TrustManager x509TrustManager = null;
				for ( TrustManager trustManager : trustManagerFactory.getTrustManagers() ) {
					if ( trustManager instanceof X509TrustManager ) {
						x509TrustManager = (X509TrustManager) trustManager;
						break;
					}
				}

				if ( x509TrustManager == null ) {
					throw new CertificateException("No X509 TrustManager available");
				}

				KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
						.getDefaultAlgorithm());
				keyManagerFactory.init(keyStore, getKeyStorePassword().toCharArray());

				X509KeyManager x509KeyManager = null;
				for ( KeyManager keyManager : keyManagerFactory.getKeyManagers() ) {
					if ( keyManager instanceof X509KeyManager ) {
						x509KeyManager = (X509KeyManager) keyManager;
						break;
					}
				}

				if ( x509KeyManager == null ) {
					throw new CertificateException("No X509 KeyManager available");
				}

				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(new KeyManager[] { x509KeyManager },
						new TrustManager[] { x509TrustManager }, null);
				solarInSocketFactory = sslContext.getSocketFactory();

			} catch ( NoSuchAlgorithmException e ) {
				throw new CertificateException("Error creating SSLContext", e);
			} catch ( KeyStoreException e ) {
				throw new CertificateException("Error creating SSLContext", e);
			} catch ( UnrecoverableKeyException e ) {
				throw new CertificateException("Error creating SSLContext", e);
			} catch ( KeyManagementException e ) {
				throw new CertificateException("Error creating SSLContext", e);
			}
		}
		return solarInSocketFactory;
	}

	private synchronized void saveKeyStore(KeyStore keyStore) {
		if ( keyStore == null ) {
			return;
		}
		File ksFile = new File(keyStorePath);
		File ksDir = ksFile.getParentFile();
		if ( !ksDir.isDirectory() && !ksDir.mkdirs() ) {
			throw new RuntimeException("Unable to create KeyStore directory: " + ksFile.getParent());
		}
		OutputStream out = null;
		try {
			String passwd = getKeyStorePassword();
			out = new BufferedOutputStream(new FileOutputStream(ksFile));
			keyStore.store(out, passwd.toCharArray());
			resetFromKeyStoreChange();
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} catch ( java.security.cert.CertificateException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} catch ( IOException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} finally {
			if ( out != null ) {
				try {
					out.flush();
					out.close();
				} catch ( IOException e ) {
					throw new CertificateException("Error closing KeyStore file: " + ksFile.getPath(), e);
				}
			}
		}
	}

	private synchronized KeyStore loadKeyStore() {
		File ksFile = new File(keyStorePath);
		InputStream in = null;
		KeyStore keyStore = null;
		String passwd = getKeyStorePassword();
		try {
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			if ( ksFile.isFile() ) {
				in = new BufferedInputStream(new FileInputStream(ksFile));
			}
			keyStore.load(in, passwd.toCharArray());
			return keyStore;
		} catch ( KeyStoreException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} catch ( NoSuchAlgorithmException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} catch ( java.security.cert.CertificateException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} catch ( IOException e ) {
			throw new CertificateException("Error creating certificate key store", e);
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch ( IOException e ) {
					log.warn("Error closing key store file {}: {}", ksFile.getPath(), e.getMessage());
				}
			}
		}
	}

	private String getSetting(String key) {
		return settingDao.getSetting(key, SETUP_TYPE_KEY);
	}

	private void saveSetting(String key, String value) {
		settingDao.storeSetting(key, SETUP_TYPE_KEY, value);
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public void setSettingDao(SettingDao settingDao) {
		this.settingDao = settingDao;
	}

	public void setNodeAlias(String nodeAlias) {
		this.nodeAlias = nodeAlias;
	}

	public void setCaAlias(String caAlias) {
		this.caAlias = caAlias;
	}

	public void setKeySize(int keySize) {
		this.keySize = keySize;
	}

	public void setCertificateService(CertificateService certificateService) {
		this.certificateService = certificateService;
	}

	public void setManualKeyStorePassword(String manualKeyStorePassword) {
		this.manualKeyStorePassword = manualKeyStorePassword;
	}

}
