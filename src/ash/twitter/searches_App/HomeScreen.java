package ash.twitter.searches_App;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

public class HomeScreen extends Activity {
	
	private SharedPreferences savedSearches;
	private TableLayout queryTableLayout;
	private EditText QueryEditText;
	private EditText tagEditText;
	public Button saveButton;
	public Button clearTagButton;
	public Button newTagbutton; 
	public Button neweditbutton; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);
		
		//Getting users saved searches through SharedPreferences
		savedSearches = getSharedPreferences("searches", MODE_PRIVATE);
		
		//Getting a refernce to the query Table layout
		queryTableLayout = (TableLayout) findViewById(R.id.QueryTableLayout);
		
		//Getting refernces to the two editbox components 
		QueryEditText = (EditText) findViewById(R.id.queryEditText);
		tagEditText = (EditText) findViewById(R.id.tagEditText);
		
		//Registering listners to the save and clear button
		saveButton = (Button) findViewById(R.id.SaveButton);
		saveButton.setOnClickListener(saveButtonListner);
		
		clearTagButton = (Button) findViewById(R.id.ClearTagsButton);
		clearTagButton.setOnClickListener(clearTagsButtonListner);
		
		refreshButtons(null);
	}


	private void refreshButtons(String newTag) {
		//store saved tags in the tags array
		String[] tags = savedSearches.getAll().keySet().toArray(new String[0]);
		Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER);
		
		if (newTag != null) {
			makeTagGUI(newTag,Arrays.binarySearch(tags, newTag));
			
		}else{
			for (int index = 0; index < tags.length; index++) {
				makeTagGUI(tags[index], index);
			}
		}
	}

	private void makeTag(String query, String tag) {
		//original querywill be null if we are modifying an excisting search 
		
		String Originalquery = savedSearches.getString(tag, null);
		
		// Getting Sharedprefernces.editor to store new tag/query pair
		SharedPreferences.Editor preferncesEditor = savedSearches.edit();
		preferncesEditor.putString(tag, query);
		preferncesEditor.apply();    // Stores the updated prefernces
		
		if (Originalquery==null) {
			refreshButtons(tag); // Adds new button for this tag
		}
		
		
	}
	
	private void makeTagGUI(String tag, int index) {
	
		// Getting a refernce to the layout inflater
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//inflate new_tag_view.xml to create new tag and edit Buttons
		View newTagview = inflater.inflate(R.layout.new_tag_view, null);
		
		//Getting newTagButton set its text and and register its listner
		newTagbutton = (Button)newTagview.findViewById(R.id.newTagButton);
		newTagbutton.setText(tag);
		newTagbutton.setOnClickListener(queryButtonListner);
		
		neweditbutton = (Button)newTagview.findViewById(R.id.newEditbutton);
		neweditbutton.setOnClickListener(editButtonListner);
		
		//add new tag and edit buttons to queryTableLayout
		queryTableLayout.addView(newTagview,index);
	}
	
	// Removes all the saved searches button from the app
	private void clearButtons(){
		// remove-all saved search Buttons
		queryTableLayout.removeAllViews();
	}
	//creates a new button and add it to the scrollview

	public OnClickListener saveButtonListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// create tag if both queryEditText and tagEdittext	are not empty
			if(QueryEditText.getText().length()>0 && tagEditText.getText().length() > 0){
				makeTag(QueryEditText.getText().toString(), tagEditText.getText().toString());
				QueryEditText.setText(""); // this clears query edit text
				tagEditText.setText("");// this clears tag edit text
				
				// hide the softkeybord
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
				builder.setTitle(R.string.missingTitle); // title bar String
				
				// simple ok button to dismiss the alert
				builder.setPositiveButton(R.string.OK, null);
				
				// Message on the dialog box to display 
				builder.setMessage(R.string.missingMessage);
				
				AlertDialog errorDialog = builder.create();
				errorDialog.show();
				
			}
		}
	};
	
	// Clears all saved searches 
	
	public OnClickListener clearTagsButtonListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// Creates a new AlertDialogue Builder
			AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
			
			// Sets the alert box title
			builder.setTitle(R.string.confirmTitle);
			
			// provides a ok button that simply dismisses the dialog 
			builder.setPositiveButton(R.string.erase,
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Clears all Saved searches from the map 
							clearButtons();
							
							// get sharedprefernce editor to clear searches
							SharedPreferences.Editor preferncesEditor = savedSearches.edit();
							preferncesEditor.clear();
							preferncesEditor.apply();
						}
					}
				);
			builder.setCancelable(true);
			builder.setNegativeButton(R.string.cancel, null);
			
			// sets the message to this alert to display 
			builder.setMessage(R.string.confirmMessage);
			// create alertdialog from the alertdialog.builder
			
			AlertDialog confirmDialog = builder.create();
			confirmDialog.show();
		}
	};
	
	public OnClickListener queryButtonListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			// get the query
			String ButtonText = ((Button)v).getText().toString();
			String query = savedSearches.getString(ButtonText, null);
			
			// create the URL corresponding to the touched buttons query
			String urlString = getString(R.string.searchURL)+query;
			Intent getUrl = new Intent(Intent.ACTION_VIEW,Uri.parse(urlString));
			
			startActivity(getUrl);
		}
	};
	
	public OnClickListener editButtonListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// get all necessary GUI components 
			
			TableRow buttonTableRow = (TableRow) v.getParent();
			Button searchButton = (Button)buttonTableRow.findViewById(R.id.newTagButton);
			
			String tag = searchButton.getText().toString();
			
			//sets edittexts to match the chosen tag and query 
			tagEditText.setText(tag);
			QueryEditText.setText(savedSearches.getString(tag, null));
		}
	};
}