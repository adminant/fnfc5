package at.zweng.emv.keys;

import java.math.BigInteger;
import java.util.Date;

/**
 * @author Johannes Zweng on 24.10.17.
 */
public class IssuerIccPublicKey extends EmvPublicKey {
    public IssuerIccPublicKey(BigInteger publicExponent, BigInteger modulus, byte[] emvCertificate, Date expirationDate) {
        super(publicExponent, modulus, emvCertificate, expirationDate);
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM_RSA;
    }

    @Override
    public String getFormat() {
        return FORMAT_ISSUER_PUBKEY;
    }
}
