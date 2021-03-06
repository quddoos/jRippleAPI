package jrippleapi.connection;

import java.math.BigDecimal;

import org.json.simple.JSONObject;

public class RippleAddressPublicInformation implements JSONSerializable {
	public String account;
	public BigDecimal xrpBalance;
	public String urlgravatar;
	public long nextTransactionSequence;
	
	@Override
	public void copyFrom(JSONObject jsonCommandResult) {
		JSONObject jsonAccountData = (JSONObject) jsonCommandResult.get("account_data");
		xrpBalance=new BigDecimal((String) jsonAccountData.get("Balance"));
		account=(String) jsonAccountData.get("Account");
		urlgravatar=(String) jsonAccountData.get("urlgravatar");
		nextTransactionSequence = (long) jsonAccountData.get("Sequence");
	}
}
