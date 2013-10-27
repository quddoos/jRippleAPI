package jrippleapi.keys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileReader;
import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import jrippleapi.beans.AccountTest;
import jrippleapi.connection.GenericJSONSerializable;
import jrippleapi.connection.RippleDaemonConnection;
import jrippleapi.serialization.RippleBinarySchema.BinaryFormatField;
import jrippleapi.serialization.RippleBinarySerializer;
import jrippleapi.serialization.RippleSerializedObject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

public class RippleSignerTest {
	@Test
	public void testDataSign() throws Exception{
		RippleBinarySerializer binSer=new RippleBinarySerializer();
		RippleSigner signer = new RippleSigner(AccountTest.getTestAccount().secret);
		JSONArray allTx = (JSONArray) new JSONParser().parse(new FileReader("testdata/unittest-tx.json"));
		for(Object obj : allTx){
			JSONObject jsonTx = (JSONObject) obj;
			String hexTx = (String) jsonTx.get("tx");
			
			ByteBuffer inputBytes=ByteBuffer.wrap(DatatypeConverter.parseHexBinary(hexTx));
			RippleSerializedObject serObj = binSer.readSerializedObject(inputBytes);
//			assertTrue("Verification failed for "+hexTx, signer.verify(serObj));
			
			serObj.removeField(BinaryFormatField.TxnSignature);
			
			byte[] hashOfTXBytes = signer.sign(serObj);
			byte[] signedBytes = binSer.writeSerializedObject(serObj).array();

			GenericJSONSerializable submitResult = new RippleDaemonConnection().submitTransaction(signedBytes);
//			assertNull(submitResult.jsonCommandResult.get("error_exception"));
			assertEquals("This sequence number has already past.", submitResult.jsonCommandResult.get("engine_result_message"));
		}
	}
}