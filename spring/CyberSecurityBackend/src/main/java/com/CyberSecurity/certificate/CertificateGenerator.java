package com.CyberSecurity.certificate;

import com.CyberSecurity.certificate.data.Issuer;
import com.CyberSecurity.certificate.data.Subject;
import com.CyberSecurity.certificate.data.CertificateType;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Component;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

@Component
public class CertificateGenerator {
    public CertificateGenerator() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate generateCertificate(Subject subject, Issuer issuer,
                                                      Date startDate, Date endDate,
                                                      String serialNumber, CertificateType type) {
        try {
            JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
            builder = builder.setProvider("BC");

            //builder je neophodan jer se privatni kljuc ne smije prenositi direktno, a potreban je za potpisivanje sertifikata
            ContentSigner contentSigner = builder.build(issuer.getPrivateKey());

            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuer.getX500Name(),
                    new BigInteger(serialNumber),
                    startDate,
                    endDate,
                    subject.getX500Name(),
                    subject.getPublicKey());
            try {
                if (CertificateType.END_ENTITY.equals(type)) {
                    certGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
                    certGen.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
                }else{
                    certGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
                    certGen.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
                }
                certGen.addExtension(Extension.subjectKeyIdentifier, false,
                        new JcaX509ExtensionUtils().createSubjectKeyIdentifier(subject.getPublicKey()));
                certGen.addExtension(Extension.authorityKeyIdentifier, false,
                        new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(issuer.getPublicKey()));
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
            X509CertificateHolder certHolder = certGen.build(contentSigner);

            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            certConverter = certConverter.setProvider("BC");

            return certConverter.getCertificate(certHolder);

        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return null;
    }
}
