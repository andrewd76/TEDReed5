package com.andrey.tedreed5;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity implements RefreshableInterface {

    private enum RSSXMLTag {
        TITLE, DATE, LINK, CONTENT, GUID, IGNORETAG, VIDEO;

    }

    private ArrayList<PostData> listData;
    private String urlString = "http://www.ted.com/themes/rss/id/6";
    private RefreshableListView postListView;
    private PostItemAdapter postAdapter;
    private boolean isRefreshLoading = true;
    private boolean isLoading = false;
    private ArrayList<String> guidList;
    private final static String PREFERENCE_FILENAME = "RssReader";
    private Intent postviewIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // check installation
        SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME,
                0);
        boolean isFirstRun = settings.getBoolean("isFirstRun", false);
        if (!isFirstRun) {

            isFirstRun = true;
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRun", isFirstRun);

            // Commit the edits!
            editor.commit();
        }

        guidList = new ArrayList<String>();
        listData = new ArrayList<PostData>();
        postListView = (RefreshableListView) this
                .findViewById(R.id.postListView);
        postAdapter = new PostItemAdapter(this, R.layout.postitem, listData);
        postListView.setAdapter(postAdapter);
        postListView.setOnRefresh(this);
        postListView.onRefreshStart();
        postListView.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            //Create the list data view
            PostData data = listData.get(arg2 - 1);

            Bundle postInfo = new Bundle();
            postInfo.putString("content", data.postContent);
            postInfo.putString("video", data.postVideo);
            if (postviewIntent == null) {
                postviewIntent = new Intent(MainActivity.this,
                        PostViewActivity.class);
            }

            postviewIntent.putExtras(postInfo);
            startActivity(postviewIntent);
        }
    };

    private class RssDataController extends
            AsyncTask<String, Integer, ArrayList<PostData>> {
        private RSSXMLTag currentTag;

        @Override
        protected ArrayList<PostData> doInBackground(String... params) {			//RSS feed parsing in the background

            String urlStr = params[0];
            InputStream is = null;
            ArrayList<PostData> postDataList = new ArrayList<PostData>();

            URL url;
            try {
                url = new URL(urlStr);
                String videoURL = "";
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();

                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int response = connection.getResponseCode();
                Log.d("debug", "The response is: " + response);
                is = connection.getInputStream();

                // parse xml with custom handmade xml parser
                //Uses XmlPullParser
                XmlPullParserFactory factory = XmlPullParserFactory
                        .newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(is, null);

                int eventType = xpp.getEventType();
                PostData pdData = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "EEE, DD MMM yyyy HH:mm:ss", Locale.US);
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {

                    } else if (eventType == XmlPullParser.START_TAG) {
                        Log.w("LOG_TAG", "START_TAG: тэга = " + xpp.getName()
                                + ", глубина = " + xpp.getDepth() + ", число атрибутов = "
                                + xpp.getAttributeCount());
                        if (xpp.getName().equals("item")) {
                            pdData = new PostData();
                            currentTag = RSSXMLTag.IGNORETAG;
                        } else if (xpp.getName().equals("title")) {
                            currentTag = RSSXMLTag.TITLE;
                        } else if (xpp.getName().equals("link")) {
                            currentTag = RSSXMLTag.LINK;
                        } else if (xpp.getName().equals("pubDate")) {
                            currentTag = RSSXMLTag.DATE;
                        } else if (xpp.getName().equals("description")) {
                            currentTag = RSSXMLTag.CONTENT;
                        } else if (xpp.getName().equals("guid")) {
                            currentTag = RSSXMLTag.GUID;
                        } else if (xpp.getName().equals("content")) {

                            if (xpp.getAttributeValue(4).equals("180")) {
                                videoURL = xpp.getAttributeValue(0);
                                pdData.postVideo = videoURL;
                            }

                            Log.w("Attributes", "videoURL = " + videoURL);

                            currentTag = RSSXMLTag.VIDEO;

                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("item")) {

                            Date postDate = dateFormat.parse(pdData.postDate);
                            pdData.postDate = dateFormat.format(postDate);
                            postDataList.add(pdData);
                        } else {
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();
                        content = content.trim();
                        if (pdData != null) {
                            switch (currentTag) {
                                case TITLE:        //saving the text data from the rss fields into the ArrayList
                                    if (content.length() != 0) {
                                        if (pdData.postTitle != null) {
                                            pdData.postTitle += content;
                                        } else {
                                            pdData.postTitle = content;
                                        }
                                    }
                                    break;
                                case LINK:
                                    if (content.length() != 0) {
                                        if (pdData.postLink != null) {
                                            pdData.postLink += content;
                                        } else {
                                            pdData.postLink = content;

                                        }
                                    }
                                    break;
                                case DATE:
                                    if (content.length() != 0) {
                                        if (pdData.postDate != null) {
                                            pdData.postDate += content;
                                        } else {
                                            pdData.postDate = content;
                                        }
                                    }
                                    break;
                                case CONTENT:
                                    if (content.length() != 0) {
                                        if (pdData.postContent != null) {
                                            pdData.postContent += content;
                                        } else {
                                            pdData.postContent = content;
                                        }
                                    }
                                    break;

                                case GUID:
                                    if (content.length() != 0) {
                                        if (pdData.postGuid != null) {
                                            pdData.postGuid += content;
                                        } else {
                                            pdData.postGuid = content;
                                        }
                                    }
                                    break;

                                default:
                                    break;
                            }
                        }
                    }

                    eventType = xpp.next();
                }
                Log.v("size of data list", String.valueOf(postDataList.size()));
            } catch (MalformedURLException e) {
                Log.e("Error", "MalformedURLException");
                // new URL exception

            } catch (ProtocolException e) {
                Log.e("Error", "ProtocolException");
                // setRequestMethod exception

            } catch (XmlPullParserException e) {
                Log.e("Error", "XmlPullParserException");
                // XmlPullParserFactory.newInstance()

            } catch (ParseException e) {
                Log.e("Error", "ParseException");
                // dateFormat.parse(pdData.postDate);

                e.printStackTrace();
            } catch (IOException e) {
                Log.e("Error", "IOException");

            }
            //returns the array list with the parsed contents of the rss feed
            return postDataList;
        }

        @Override
        protected void onPostExecute(ArrayList<PostData> result) {   //onPost insert data in arrayList

            boolean isupdated = false;
            for (int i = 0; i < result.size(); i++) {
                // check if the post is already in the list
                if (guidList.contains(result.get(i).postGuid)) {
                    continue;
                } else {
                    isupdated = true;
                    guidList.add(result.get(i).postGuid);
                }

                if (isRefreshLoading) {
                    listData.add(i, result.get(i));

                } else {
                    listData.add(result.get(i));

                }
            }

            if (isupdated) {
                postAdapter.notifyDataSetChanged();
            }

            isLoading = false;

            if (isRefreshLoading) {
                postListView.onRefreshComplete();
            } else {
                postListView.onLoadingMoreComplete();
            }

            super.onPostExecute(result);
        }
    }

    @Override
    public void startFresh() {
        //load or reload the rss feed
        if (!isLoading) {
            isRefreshLoading = true;
            isLoading = true;
            new RssDataController().execute(urlString);
        } else {
            postListView.onRefreshComplete();
        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
