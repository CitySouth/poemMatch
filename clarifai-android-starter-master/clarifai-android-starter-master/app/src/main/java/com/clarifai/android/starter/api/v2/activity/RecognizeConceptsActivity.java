package com.clarifai.android.starter.api.v2.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ViewSwitcher;
import butterknife.BindView;
import butterknife.OnClick;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import com.clarifai.android.starter.api.v2.App;
import com.clarifai.android.starter.api.v2.ClarifaiUtil;
import com.clarifai.android.starter.api.v2.R;
import com.clarifai.android.starter.api.v2.adapter.RecognizeConceptsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class RecognizeConceptsActivity extends BaseActivity {

  public static final int PICK_IMAGE = 100;
  private JSONObject testJson;
  private ArrayList<String> poem_list = new ArrayList<>();
  private JSONArray array = new JSONArray();
  private ListView lv;

  // the list of results that were returned from the API
  @BindView(R.id.resultsList) RecyclerView resultsList;



  // the view where the image the user selected is displayed
  @BindView(R.id.image) ImageView imageView;

  // switches between the text prompting the user to hit the FAB, and the loading spinner
  @BindView(R.id.switcher) ViewSwitcher switcher;

  // the FAB that the user clicks to select an image
  @BindView(R.id.fab) View fab;

  @NonNull private final RecognizeConceptsAdapter adapter = new RecognizeConceptsAdapter();
  private ArrayAdapter arrayAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    lv = (ListView) findViewById(R.id.resultsList);
    super.onCreate(savedInstanceState);
    load_json();

  }

  @Override protected void onStart() {
    super.onStart();

    resultsList.setLayoutManager(new LinearLayoutManager(this));
    resultsList.setAdapter(adapter);
  }

  @OnClick(R.id.fab)
  void pickImage() {
    startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_IMAGE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != RESULT_OK) {
      return;
    }
    switch(requestCode) {
      case PICK_IMAGE:
        final byte[] imageBytes = ClarifaiUtil.retrieveSelectedImage(this, data);
        if (imageBytes != null) {
          onImagePicked(imageBytes);
        }
        break;
    }
  }

  private void onImagePicked(@NonNull final byte[] imageBytes) {
    // Now we will upload our image to the Clarifai API
    setBusy(true);

    // Make sure we don't show a list of old concepts while the image is being uploaded
    ArrayList empty_list = new ArrayList();
//    adapter.setData(Collections.<Concept>emptyList());
    adapter.setData(empty_list);
    new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {
      @Override protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Void... params) {
        // The default Clarifai model that identifies concepts in images
        final ConceptModel generalModel = App.get().clarifaiClient().getDefaultModels().generalModel();

        // Use this model to predict, with the image that the user just selected as the input
        return generalModel.predict()
            .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
            .executeSync();
      }

      @Override protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {
        setBusy(false);
        if (!response.isSuccessful()) {
          showErrorSnackbar(R.string.error_while_contacting_api);
          return;
        }
        final List<ClarifaiOutput<Concept>> predictions = response.get();
        if (predictions.isEmpty()) {
          showErrorSnackbar(R.string.no_results_from_api);
          return;
        }

        match_poem(predictions);

        adapter.setData(poem_list);
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
      }

      private void showErrorSnackbar(@StringRes int errorString) {
        Snackbar.make(
            root,
            errorString,
            Snackbar.LENGTH_INDEFINITE
        ).show();
      }
    }.execute();
  }


  @Override protected int layoutRes() { return R.layout.activity_recognize; }

  private void setBusy(final boolean busy) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        switcher.setDisplayedChild(busy ? 1 : 0);
        imageView.setVisibility(busy ? GONE : VISIBLE);
        fab.setEnabled(!busy);
      }
    });
  }

  //load json
  private void load_json() {
    try {
      InputStreamReader isr = new InputStreamReader(getAssets().open("poem.json"), "UTF-8");
      BufferedReader br = new BufferedReader(isr);
      String line;
      StringBuilder builder = new StringBuilder();
      while ((line = br.readLine()) != null) {
        builder.append(line);
      }
      br.close();
      isr.close();
      testJson = new JSONObject(builder.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //match_poem
  private void match_poem(List<ClarifaiOutput<Concept>> predictions) {
    poem_list = new ArrayList<>();
    try {
      List<Concept> results = predictions.get(0).data();

      //the list of the title of poems
      List title_list = new ArrayList();
      for (Iterator<String> iterator = testJson.keys(); iterator.hasNext(); ) {
        String key = iterator.next();
        title_list.add(key);
      }

      boolean found = false;
      for (int i = 0; i < results.size() && !found; i++) {
//        for (int j = 0; j < title_list.size(); j++) {
//          String str = title_list.get(j).toString();
        String check = results.get(i).name();
        if (title_list.contains(check)) {
          int idx = title_list.indexOf(check);

          array = testJson.getJSONArray(title_list.get(idx).toString());
          found = true;
          break;

        }

      }


      for (int i = 0; i < array.length(); i++) {
        poem_list.add(array.getString(i));
      }

    } catch(Exception e){
      e.printStackTrace();
    }

  }

}
