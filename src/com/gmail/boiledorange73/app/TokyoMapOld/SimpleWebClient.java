package com.gmail.boiledorange73.app.TokyoMapOld;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class SimpleWebClient {
    public static interface SimpleWebClientListener {
        public void onError(SimpleWebClient client, String url, String message);
        public void onArrive(SimpleWebClient client, String url, String result);
        public void onPreExecuteOnUiThread(SimpleWebClient client);
        public void onCanceledOnUiThread(SimpleWebClient client);
        public void onFinishedOnUiThread(SimpleWebClient client, int result);
    }

    private static class Task extends AsyncTask<String, Integer , Integer> {
        private HttpClient mHttpClient = null;
        private SimpleWebClientListener mListener;
        private SimpleWebClient mClient;

        public Task(SimpleWebClient client, SimpleWebClientListener listener) {
            this.mClient = client;
            this.mListener = listener;
            this.mHttpClient = new DefaultHttpClient();
        }

        private void fireArrive(String url, String result) {
            if( this.mListener != null ) {
                this.mListener.onArrive(this.mClient, url, result);
            }
        }

        private void fireError(String url, String message) {
            if( this.mListener != null ) {
                this.mListener.onError(this.mClient, url, message);
            }
        }

        public void expire() {
            this.cancel(true);
            if( this.mHttpClient != null ) {
                ClientConnectionManager connM = this.mHttpClient.getConnectionManager();
                if( connM != null ) {
                    connM.shutdown();
                }
                this.mHttpClient = null;
            }
            this.mListener = null;
        }

        @Override
        protected void finalize() throws Throwable {
            this.expire();
            super.finalize();
        }
        @Override
        protected void onPreExecute() {
            if( this.mListener != null ) {
                this.mListener.onPreExecuteOnUiThread(this.mClient);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            int cnt = 0;
            int acnt = 0;
            if( params != null ) {
                for( String url : params ) {
                    // Canceled CP
                    if( this.isCancelled() ) {
                        return acnt;
                    }
                    if( url != null ) {
                        // http params, timeout: 10sec
                        HttpParams httpParams = this.mHttpClient.getParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
                        HttpConnectionParams.setSoTimeout(httpParams, 1000);
                        HttpResponse httpResponse = null;
                        try {
                            if( this.mHttpClient != null ) {
                                httpResponse = this.mHttpClient.execute(new HttpGet(url));
                                if (httpResponse != null
                                        && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                    HttpEntity httpEntity = httpResponse.getEntity();
                                    String result = EntityUtils.toString(httpEntity);
                                    // Canceled CP
                                    if( this.isCancelled() ) {
                                        return acnt;
                                    }
                                    this.fireArrive(url, result);
                                    acnt++;
                                }
                                else {
                                    // Canceled CP
                                    if( this.isCancelled() ) {
                                        return acnt;
                                    }
                                    this.fireError(url, "Client cannot work.");
                                }
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                            // Canceled CP
                            if( this.isCancelled() ) {
                                return acnt;
                            }
                            this.fireError(url, e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            // Canceled CP
                            if( this.isCancelled() ) {
                                return acnt;
                            }
                            this.fireError(url, e.getMessage());
                        }
                    }
                    this.publishProgress(++cnt);
                }
            }
            return acnt;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if( this.mListener != null ) {
                this.mListener.onFinishedOnUiThread(this.mClient, result != null ? result : -1);
            }
        }

        @Override
        protected void onCancelled() {
            if( this.mListener != null ) {
                this.mListener.onCanceledOnUiThread(this.mClient);
            }
        }
    }

    private Task mTask;

    public void load(String url, SimpleWebClientListener listener) {
        this.mTask = new Task(this,listener);
        this.mTask.execute(url);
    }
    
    public void cancel() {
        if( this.mTask != null ) {
            if( this.mTask.getStatus() != AsyncTask.Status.FINISHED) {
                this.mTask.cancel(true);
            }
            this.mTask = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.cancel();
        super.finalize();
    }
}
