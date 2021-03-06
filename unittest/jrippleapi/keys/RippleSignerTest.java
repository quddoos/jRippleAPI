package jrippleapi.keys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import jrippleapi.TestUtilities;
import jrippleapi.connection.GenericJSONSerializable;
import jrippleapi.connection.RippleDaemonWebsocketConnection;
import jrippleapi.core.RipplePrivateKey;
import jrippleapi.serialization.RippleBinaryObject;
import jrippleapi.serialization.RippleBinarySerializer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

public class RippleSignerTest {
	@Test
	public void testSubmitSignedTransaction() throws Exception{
		RippleBinarySerializer binSer=new RippleBinarySerializer();
		RipplePrivateKey privateKey = TestUtilities.getTestSeed().getPrivateKey(0);
		RippleSigner signer = new RippleSigner(privateKey);
		JSONArray allTx = (JSONArray) new JSONParser().parse(new FileReader("testdata/unittest-tx.json"));
		for(Object obj : allTx){
			JSONObject jsonTx = (JSONObject) obj;
			String hexTx = (String) jsonTx.get("tx");
			
			ByteBuffer inputBytes=ByteBuffer.wrap(DatatypeConverter.parseHexBinary(hexTx));
			RippleBinaryObject originalSignedRBO = binSer.readBinaryObject(inputBytes);
			assertTrue("Verification failed for "+hexTx, signer.isSignatureVerified(originalSignedRBO));
			
			RippleBinaryObject reSignedRBO = signer.sign(originalSignedRBO.getUnsignedCopy());
			byte[] signedBytes = binSer.writeBinaryObject(reSignedRBO).array();
			GenericJSONSerializable submitResult = new RippleDaemonWebsocketConnection(RippleDaemonWebsocketConnection.RIPPLE_SERVER_URL).submitTransaction(signedBytes);
//			assertNull(submitResult.jsonCommandResult.get("error_exception"));
			assertEquals("This sequence number has already past.", submitResult.jsonCommandResult.get("engine_result_message"));
			assertTrue(signer.isSignatureVerified(reSignedRBO));
		}
	}
}
