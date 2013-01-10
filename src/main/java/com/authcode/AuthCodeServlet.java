package com.authcode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;

/**
 * User: Dori Ding
 * Date: 13-1-10
 * Time: 12:06
 */
public class AuthCodeServlet extends HttpServlet {

    private final static HttpClient client = new DefaultHttpClient();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cookie = req.getParameter("cookie1");
        String callback = req.getParameter("callback");

        resp.setContentType("application/json");
        System.out.println("========" + cookie);

        try {
            createTotalHttpGet("https://dynamic.12306.cn/otsweb/passCodeAction.do?rand=sjrand&0." + new Random().nextInt(), cookie);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        PrintWriter writer = resp.getWriter();
        writer.print("setAuthCode({\"code\":\"1234\"})");
        writer.flush();
        writer.close();
    }


    public HttpGet createTotalHttpGet(String url, String cookie) throws URISyntaxException, IOException, HttpException {
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "GBK,utf-8;q=0.7,*;q=0.3");
        get.setHeader("Accept-Encoding", "gzip,deflate,sdch");
        get.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
        get.setHeader("Connection", "keep-alive");
        get.setHeader("Cookie", cookie);
        get.setHeader("Host", "dynamic.12306.cn");
        get.setHeader("Referer", "https://dynamic.12306.cn/otsweb/loginAction.do?method=init");
        get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11");

//        HttpResponse response = client.execute(get);
        HttpResponse response = wrapClient(client).execute(get);
        HttpEntity entity = response.getEntity();
        String img = dump(entity);
        return get;
    }

    private static String dump(HttpEntity entity) throws IOException {

        InputStream is = entity.getContent();
        FileOutputStream fos = new FileOutputStream("c:/3.jpg");

        byte buf[] = new byte[1024];
        int numread = 0;
        do {
            // Start reading data from the URL stream
            numread = is.read(buf);
            if (numread < 0) {
                //System.out.println("the end !!!!");
                break;
            }
            fos.write(buf, 0, numread);

        } while (true);

        fos.close();

        return "";
    }

    /**
     * 以URL方式发送数据
     *
     * @param urlStr
     *            发送地址
     * @param contentStr
     *            发送内容
     * @param charset
     *            发送字符集
     * @param sResult
     *            返回数据Buffer
     * @return boolean 发送是否成功
     */
    public boolean sendStrOfPost(String urlStr, String contentStr, String charset, StringBuffer sResult) {
        boolean bResult = false;
        String charsetName = charset;
        URL url = null;
        HttpURLConnection httpConnection = null;
        InputStream httpIS = null;
        java.io.BufferedReader http_reader = null;
        try {
            url = new URL(urlStr);
            httpConnection = (HttpURLConnection) url.openConnection();


            httpConnection.setRequestMethod("POST"); // POST方式提交数据
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty("Content-Length", String.valueOf(contentStr.getBytes().length));
            PrintWriter out = null;
            out = new PrintWriter(new OutputStreamWriter(httpConnection.getOutputStream(), charsetName));// 此处改动
            // 发送请求
            out.print(contentStr);
            out.flush();
            out.close();
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 发送正常
                bResult = true;

                // 读取数据
                httpIS = httpConnection.getInputStream();
                http_reader = new java.io.BufferedReader(new java.io.InputStreamReader(httpIS, charsetName));
                String line = null;
                while ((line = http_reader.readLine()) != null) {
                    if (sResult.length() > 0) {
                        sResult.append("\n");
                    }
                    sResult.append(line);
                }
            } else {
                System.out.println("[URL][response][failure][code : " + responseCode + " ]");
            }
        } catch (IOException e) {
            System.out.println("[HttpUtil]sendStrOfPost error" + e);
        }
        finally {
            try {
                if (http_reader != null)
                    http_reader.close();
                if (httpIS != null)
                    httpIS.close();
                if (httpConnection != null)
                    httpConnection.disconnect();
            } catch (IOException e) {
                System.out.println("[HttpUtil]finally error" + e);
            }
        }

        return bResult;
    }

    public static HttpClient wrapClient(HttpClient base) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                ctx.init(null, new TrustManager[]{tm}, null);

                SSLSocketFactory ssf = new SSLSocketFactory(ctx);
                ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                ClientConnectionManager ccm = base.getConnectionManager();
                SchemeRegistry sr = ccm.getSchemeRegistry();
                sr.register(new Scheme("https", ssf, 443));
                return new DefaultHttpClient(ccm, base.getParams());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

}
