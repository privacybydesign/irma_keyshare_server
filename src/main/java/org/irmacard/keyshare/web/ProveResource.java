package org.irmacard.keyshare.web;

import org.irmacard.api.common.util.GsonUtil;
import org.irmacard.credentials.idemix.proofs.ProofP;
import org.irmacard.credentials.idemix.proofs.ProofPCommitmentMap;
import org.irmacard.credentials.info.InfoException;
import org.irmacard.credentials.info.KeyException;
import org.irmacard.credentials.info.PublicKeyIdentifier;
import org.irmacard.keyshare.common.IRMAHeaders;
import org.irmacard.keyshare.web.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.List;

@Path("prove")
public class ProveResource {
	public static final String JWT_SUBJECT = "ProofP";

	private static Logger logger = LoggerFactory.getLogger(ProveResource.class);

	@GET
	@Path("/publickey")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPublickey() {
		return GsonUtil.getGson().toJson(KeyshareConfiguration.getInstance().getJwtPublicKey().getEncoded());
	}

	@POST
	@Path("/getCommitments")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ProofPCommitmentMap getCommitments(List<PublicKeyIdentifier> pkids,
			@HeaderParam(IRMAHeaders.AUTHORIZATION_OLD) String oldJwt,
			@HeaderParam(IRMAHeaders.AUTHORIZATION) String jwt)
			throws InfoException, KeyException {
		if (jwt == null) jwt = oldJwt;

		User u = VerificationResource.authorizeUser(jwt);

		logger.info("Answering proof request for: {}", u.getUsername());

		return u.generateCommitments(pkids);
	}

	@POST
	@Path("/getResponse")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String getResponse(BigInteger challenge,
			@HeaderParam(IRMAHeaders.AUTHORIZATION_OLD) String oldJwt,
			@HeaderParam(IRMAHeaders.AUTHORIZATION) String jwt) {
		if (jwt == null) jwt = oldJwt;

		User u = VerificationResource.authorizeUser(jwt);

		logger.info("Gotten challenge for user: {}", u.getUsername());

		ProofP proof = u.buildProofP(challenge);
		return BaseVerifier.getSignedJWT("ProofP", proof, JWT_SUBJECT);
	}
}
