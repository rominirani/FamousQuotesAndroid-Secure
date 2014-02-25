package com.mindstorm.famousquotesandroid;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.mindstorm.famousquotes.entity.quoteendpoint.Quoteendpoint;
import com.mindstorm.famousquotes.entity.quoteendpoint.model.Quote;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class AddQuoteActivity extends Activity {
	
	private static final int REQUEST_ACCOUNT_PICKER = 2;
	EditText editAuthorName;
	EditText editMessage;
	private SharedPreferences settings;
	private String accountName;
	private GoogleAccountCredential credential;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addquote);
		
		editAuthorName = (EditText)findViewById(R.id.editAuthorName);
		editMessage = (EditText)findViewById(R.id.editMessage);
		
		//Event Listener for About App button
		Button btnAddQuote = (Button)findViewById(R.id.btnAddQuote);
		btnAddQuote.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//Check if values are provided
				String txtAuthorName = editAuthorName.getText().toString().trim();
				String txtMessage = editMessage.getText().toString().trim();
				
				if ((txtAuthorName.length() == 0) || (txtMessage.length() == 0)) {
					Toast.makeText(AddQuoteActivity.this, "You need to provide values for Author and Message", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//Go ahead and perform the transaction
				String[] params = {txtAuthorName,txtMessage};
				new AddQuoteAsyncTask(AddQuoteActivity.this).execute(params);
				
			}
		});
		
		//Account stuff
		settings = getSharedPreferences("FamousQuotesAndroid", 0);
		credential = GoogleAccountCredential.usingAudience(this,"server:client_id:756161739003-0l5c7ptti2j42l28j5anijr5mukks835.apps.googleusercontent.com");
		setAccountName(settings.getString("ACCOUNT_NAME", null));
		if (credential.getSelectedAccountName() != null) {
			 // Already signed in, begin app!
			Toast.makeText(getBaseContext(), "Logged in with : " + credential.getSelectedAccountName(), Toast.LENGTH_SHORT).show();
			//Toast.makeText(getBaseContext(), GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()),Toast.LENGTH_SHORT).show();
		} else {
			 // Not signed in, show login window or request an account.
			chooseAccount();
		}
	}
	
	private class AddQuoteAsyncTask extends AsyncTask<String, Void, Quote>{
		  Context context;
		  private ProgressDialog pd;

		  public AddQuoteAsyncTask(Context context) {
		    this.context = context;
		  }
		  
		  protected void onPreExecute(){ 
		     super.onPreExecute();
		          String accountName = settings.getString("ACCOUNT_NAME", null);
		          pd = new ProgressDialog(context);
		          pd.setMessage("[" + accountName + "]" + " Adding the Quote...");
		          pd.show();    
		  }

		  protected Quote doInBackground(String... params) {
			  Quote response = null;
		    try {
		    	Quoteendpoint.Builder builder = new Quoteendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential);
				Quoteendpoint service =  builder.build();
				Quote quote = new Quote();
				quote.setAuthor(params[0]);
				quote.setMessage(params[1]);
				response = service.insertQuote(quote).execute();
				Log.d("Response from call", response.getMessage());
		    } catch (Exception e) {
		      Log.d("Could not Add Quote", e.getMessage(), e);
		    }
		    return response;
		  }

		  protected void onPostExecute(Quote quote) {
			  //Clear the progress dialog and the fields
			  pd.dismiss();
			  editMessage.setText("");
			  editAuthorName.setText("");
			  
			  //Display success message to user
			  Toast.makeText(getBaseContext(), "Quote added succesfully", Toast.LENGTH_SHORT).show();
		  }
		}
	
	// setAccountName definition
	private void setAccountName(String accountName) {
	 SharedPreferences.Editor editor = settings.edit();
	 editor.putString("ACCOUNT_NAME", accountName);
	 editor.commit();
	 credential.setSelectedAccountName(accountName);
	 this.accountName = accountName;
	}
	
	void chooseAccount() {
		  startActivityForResult(credential.newChooseAccountIntent(),
		    REQUEST_ACCOUNT_PICKER);
		}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	   Intent data) {
	 super.onActivityResult(requestCode, resultCode, data);
	 switch (requestCode) {
	   case REQUEST_ACCOUNT_PICKER:
	     if (data != null && data.getExtras() != null) {
	       String accountName =
	           data.getExtras().getString(
	               AccountManager.KEY_ACCOUNT_NAME);
	       if (accountName != null) {
	         setAccountName(accountName);
	         SharedPreferences.Editor editor = settings.edit();
	         editor.putString("ACCOUNT_NAME", accountName);
	         editor.commit();
	         // User is authorized.
	       }
	     }
	     break;
	 }
	}


}
