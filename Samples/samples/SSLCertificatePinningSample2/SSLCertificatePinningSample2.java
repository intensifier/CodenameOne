package com.codename1.samples;

import com.codename1.io.ConnectionRequest;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.ui.Toolbar;
import java.io.IOException;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.io.NetworkEvent;
import com.codename1.io.NetworkManager;
import com.codename1.ui.Button;
import com.codename1.ui.CN;
import java.io.OutputStream;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename
 * One</a> for the purpose of building native mobile applications using Java.
 */
public class SSLCertificatePinningSample2 {

    private Form current;
    private Resources theme;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature
        Log.bindCrashProtection(true);

        addNetworkErrorListener(err -> {
            // prevent the event from propagating
            err.consume();
            if (err.getError() != null) {
                Log.e(err.getError());
            }
            Log.sendLogAsync();
            Dialog.show("Connection Error", "There was a networking error in the connection to " + err.getConnectionRequest().getUrl(), "OK", null);
        });
    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        String url = "https://weblite.ca/tmp/postecho.php";
        Form hi = new Form("Hi World", BoxLayout.y());
        Button test = new Button("Run Test");
        test.addActionListener(e -> {
            ConnectionRequest req = new ConnectionRequest() {
                @Override
                protected void buildRequestBody(OutputStream os) throws IOException {
                    byte[] encryptedArray = SecurityHelper.getInstance().encrypt("{\"hello\" : \"world\"}\n");
                    os.write(encryptedArray);
                    os.flush();
                    CN.callSerially(()->{
                        hi.add(new Label("Built request body"));
                        hi.revalidateWithAnimationSafety();
                    });
                    //Stage 3
                }

                @Override
                protected void checkSSLCertificates(ConnectionRequest.SSLCertificate[] certificates) {
                    CN.callSerially(()->{
                        hi.add(new Label("Checked SSL Certificates"));
                        hi.revalidateWithAnimationSafety();
                    });
                }

                @Override
                protected void handleException(Exception err) {
                    err.printStackTrace();
                    CN.callSerially(()->{
                        hi.add(new Label("Exception: "+err.getMessage()));
                        hi.revalidateWithAnimationSafety();
                    }); 
                    
                }

                @Override
                protected void handleErrorResponseCode(int code, String message) {
                    super.handleErrorResponseCode(code, message);
                    CN.callSerially(()->{
                        hi.add(new Label("Response code: "+200+", "+message));
                        hi.revalidateWithAnimationSafety();
                    });
                }

                @Override
                protected void handleIOException(IOException err) {
                    System.out.println("io ex");
                    super.handleIOException(err); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                protected void handleRuntimeException(RuntimeException err) {
                    System.out.println("runtime ex");
                    super.handleRuntimeException(err); //To change body of generated methods, choose Tools | Templates.
                }
                
                
                
                
            };

            req.setCheckSSLCertificates(true);
            req.setWriteRequest(true);
            req.setTimeout(90000);
            req.setSilentRetryCount(2);
            req.setFailSilently(true);
            req.setPost(true);
            req.setUrl(url);
            req.setContentType("application/octet-stream; charset=utf-8; ");
            //Stage 1
            NetworkManager.getInstance().addToQueueAndWait(req);
            hi.add(new Label("Response code: "+req.getResponseCode()));
            hi.revalidateWithAnimationSafety();
        });
        hi.add(test);
        hi.show();
    }

    public void stop() {
        current = getCurrentForm();
        if (current instanceof Dialog) {
            ((Dialog) current).dispose();
            current = getCurrentForm();
        }
    }

    public void destroy() {
    }
    
    private static class SecurityHelper {
        private static SecurityHelper instance;
        static SecurityHelper getInstance() {
            if (instance == null) {
                instance = new SecurityHelper();
            }
            return instance;
        }
        
        byte[] encrypt(String jsonPayload) {
            try {
                return jsonPayload.getBytes("UTF-8");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
