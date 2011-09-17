/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sunny.newmovierecommendation;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Util;
import com.sunny.newmovierecommendation.SessionEvents.AuthListener;
import com.sunny.newmovierecommendation.SessionEvents.LogoutListener;
import com.sunny.screen.MovieRecommendationScreen;
import com.sunny.screen.MovieScreen;


public class NewMovieRecommendationActivity extends ListActivity {

    // Your Facebook Application ID must be set before running this example
    // See http://www.facebook.com/developers/createapp.php
    public static final String APP_ID = "175729095772478";

    private LoginButton mLoginButton;
    private TextView mText;
    private Button mRequestButton;
    private String gender;
    ListView lv;

    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be " +
                    "specified before running this example: see Example.java");
        }

        setContentView(R.layout.main);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mText = (TextView) NewMovieRecommendationActivity.this.findViewById(R.id.txt);
        mRequestButton = (Button) findViewById(R.id.requestButton);
        
        
       	mFacebook = new Facebook(APP_ID);
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);

        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(this, mFacebook);

        mRequestButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mAsyncRunner.request("me", new SampleRequestListener());
            }
        });
        mRequestButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);

      //helper list view function
       // movieListView();
       
    }     
            
       

    private void movieListView() {
		// TODO Auto-generated method stub
    	String[] buttons = getResources().getStringArray(R.array.main_buttons);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_view, buttons));

         lv = getListView();
         lv.setTextFilterEnabled(true);
        // lv.setVisibility(View.INVISIBLE); 
         
         lv.setOnItemClickListener(new OnItemClickListener() {
           
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         		MovieScreen.setListValue(id);
         		
         		if(id != 0){
         			Intent i = new Intent(NewMovieRecommendationActivity.this, MovieScreen.class);
         			startActivity(i);
         		}
         		else{
         			
         			RecommendGenre recGenre = new RecommendGenre(gender);
         			MovieRecommendationScreen movieRec = new MovieRecommendationScreen();
         			movieRec.setList(recGenre.recommendGenre());
         			Intent i = new Intent(NewMovieRecommendationActivity.this, MovieRecommendationScreen.class);
         			startActivity(i);
         		}
         	  
              }
         });
         
	}



	@Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
            mText.setText("You have logged in! ");
            mRequestButton.setVisibility(View.VISIBLE);
           // lv.setVisibility(View.VISIBLE); 
            movieListView();
        }

        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }

        public void onLogoutFinish() {
            mText.setText("You have logged out! ");
            mRequestButton.setVisibility(View.INVISIBLE);
            lv.setVisibility(View.INVISIBLE); 
        }
    }

    public class SampleRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: executed in background thread
                Log.d("Facebook-Example", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                final String name = json.getString("name");
                gender = json.getString("gender");
                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                NewMovieRecommendationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mText.setText("Hello there, " + name + "!");
                    }
                });
            } catch (JSONException e) {
                Log.w("Facebook-Example", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
            }
        }
    }

   
}
