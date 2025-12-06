package com.progra3.cafeteria_api.afip.wsass;

import com.progra3.cafeteria_api.config.AfipConfig;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class TraSigner {

    private final AfipConfig afipConfig;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public byte[] signTra(String traXml) {
        try {
            KeyStore keyStore = loadKeyStore();
            String alias = Collections.list(keyStore.aliases()).getFirst();

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(
                    alias,
                    afipConfig.getCertPassword().toCharArray()
            );
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            byte[] data = traXml.getBytes(StandardCharsets.UTF_8);

            // Create CMS (PKCS#7) signed data
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            JcaCertStore certStore = new JcaCertStore(
                Collections.singletonList(certificate)
            );
            gen.addCertificates(certStore);

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(privateKey);

            gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder()
                        .setProvider("BC")
                        .build()
                ).build(signer, certificate)
            );

            CMSTypedData cmsData = new org.bouncycastle.cms.CMSProcessableByteArray(data);
            CMSSignedData signedData = gen.generate(cmsData, true);

            return signedData.getEncoded();

        } catch (Exception e) {
            throw new IllegalStateException("Error signing TRA for WSAA", e);
        }
    }

    private KeyStore loadKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            String path = afipConfig.getCertPath();
            InputStream is;

            if (path.startsWith("classpath:")) {
                String cp = path.replace("classpath:", "");
                ClassPathResource resource = new ClassPathResource(cp);
                is = resource.getInputStream();
            } else {
                is = Files.newInputStream(Path.of(path));
            }

            ks.load(is, afipConfig.getCertPassword().toCharArray());
            return ks;
        } catch (Exception e) {
            throw new IllegalStateException("Error loading AFIP PKCS12 keystore", e);
        }
    }
}

