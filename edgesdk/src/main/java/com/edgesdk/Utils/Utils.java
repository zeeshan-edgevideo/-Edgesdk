/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.edgesdk.Utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.util.Log;

import com.edgesdk.models.TemporaryWallet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

/**
 *
 * @author HP
 */
public class Utils {

    private static String BaseUrl_For_WalletForwarding = "https://eat.edgevideo.com:8081";
    private static String BaseUrl_For_FourDigitCodeProcessing = "https://eat.edgevideo.com:8080";

    public static Long getCurrentTimeInMilliSec() {
        String ts = String.valueOf(System.currentTimeMillis());
        return Long.parseLong(ts);
    }

    public static JsonNode parser(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode converted = objectMapper.readTree(message);
            return converted;
        } catch (Exception e) {
            return null;
        }
    }
    public static JsonNode jsonFileRead(String filePath){
        URL url = null;
        try {
            url = new URL(filePath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), UTF_8))) {
            String str = null;
            while (true) {
                try {
                    if (!((str = bufferedReader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                builder.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonStr = builder.toString();
        JsonNode jsonNode = null;
        if(jsonStr!=null)
            jsonNode = parser(jsonStr);

        return jsonNode;
    }

    public static JsonNode makeGetRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection cons = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            cons.getInputStream()));

            String inputLine = in.readLine();

            System.out.println("inputLine : "+inputLine);
            JsonNode convertedData = parser(inputLine);
            if (convertedData != null) {
                in.close();
                return convertedData;
            } else {
                System.out.println("Converted data is null after fetching.");
                in.close();
                return null;
            }
        } catch (Exception e) {
            Log.e("error","Error while making get request for url : "+urlString+" |  error :"+e.toString());
            return null;
        }

    }

    public static JsonNode makePostRequest(String serverUrl, JSONObject data) {
        try {
            URL obj = new URL(serverUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // Set request method and headers
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            // Send POST request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data.toString());
            wr.flush();
            wr.close();

            // Get response
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            // Log response
            Log.i(LogConstants.Wallet_Forwarding, "Response: " + response.toString());

            return parser(response.toString());
        } catch (Exception e) {
            return null;
        }

    }

    public static void CopyData(JsonNode a, JsonNode b) {
        if (a != null && b != null) {
            ((ObjectNode) a.get("result")).put("estimate", b.get("result").get("estimate"));
            ((ObjectNode) a.get("result")).put("minutes", b.get("result").get("minutes"));
            ((ObjectNode) a.get("result")).put("balance", b.get("result").get("balance"));
            ((ObjectNode) a.get("result")).put("maxBalance", b.get("result").get("maxBalance"));
            ((ObjectNode) a.get("result")).put("maxBalance", b.get("result").get("maxBalance"));
            ((ObjectNode) a.get("result")).put("earned", b.get("result").get("earned"));
            //((ObjectNode) a.get("result")).put("hoursUntilStaking", b.get("result").get("hoursUntilStaking"));
            ((ObjectNode) a).put("t", b.get("t"));
            System.out.println("Data Copied successfully");

        } else {
            Log.e("error a",a+"");
            Log.e("error b",b+"");
            Log.e("error","Error while making copy of current data to old on.");
        }
    }
    public static double Lerp(double val1, double val2, double f) {
        f = (float) Math.max(0.0, Math.min(1.0, f));
        double ans = (val1 * (1.0 - f)) + (val2 * f);
        return ans;
    }

    public static double CalculateEarnedAmount(JsonNode oldData, JsonNode currentData, double lerpFactor) {
        double earnedAmount = Lerp(
                Double.parseDouble(oldData.get("result").get("earned") + ""),
                Double.parseDouble(currentData.get("result").get("earned") + ""),
                lerpFactor);
        return earnedAmount;
    }

    public static double CalculateStakedPercentage(JsonNode oldData, JsonNode currentData, double lerpFactor) {
        double lerpCalcultedValue = Lerp(
                Double.parseDouble(oldData.get("result").get("minutes") + ""),
                Double.parseDouble(currentData.get("result").get("minutes") + ""),
                lerpFactor);
        //Log.d("lerpCalcultedValue",lerpCalcultedValue+"");

        double lerpValue = Math.min(1440, lerpCalcultedValue) / 1440;
        return lerpValue;
    }

    public static double CalculateStakeAmount(JsonNode oldData, JsonNode currentData, double lerpFactor, double stakedPercentage) {
        double amount = stakedPercentage * (Lerp(
                Double.parseDouble(oldData.get("result").get("balance") + ""),
                Double.parseDouble(currentData.get("result").get("balance") + ""),
                lerpFactor));
        return amount;
    }

    @SuppressLint("Range")
    public static double CalculateEstimate(JsonNode oldData, JsonNode currentData, double lerpFactor, double stakedPercentage) {
        double lerpValue = Lerp(
                Double.parseDouble(oldData.get("result").get("estimate") + ""),
                Double.parseDouble(currentData.get("result").get("estimate") + ""),
                lerpFactor);
        return stakedPercentage*lerpValue;
    }

    public static double CalculateHoursUntilStaking(JsonNode oldData, JsonNode currentData, double lerpFactor) {
        try{
            double hours = Lerp(
                    Double.parseDouble(oldData.get("result").get("hoursUntilStaking") + ""),
                    Double.parseDouble(currentData.get("result").get("hoursUntilStaking") + ""),
                    lerpFactor);
            return hours;
        }catch (Exception e){
            return 0.0;
        }

    }
    public static String FormateHours(double hours) {
        if (hours > 24) {
            //turn days.
            return FormateDecimal((hours/24), 2)+" DAYS";
        } else if (hours > 1) {
            //return hours
            return FormateDecimal(hours, 2)+" HOURS";
        } else {
            //return minutes
            return FormateDecimal((hours*60), 2)+" MINUTES";
        }
    }
    public static float FormateDecimal(double value, int places) {
        try {
            String valueStr = value + "";
            if (valueStr.contains(",")) {
                valueStr = valueStr.replace(",", "");
                value = Double.parseDouble(valueStr);
            }
            DecimalFormat valueFormater = new DecimalFormat();
            valueFormater.setMaximumFractionDigits(places);
            return Float.parseFloat(valueFormater.format(value));
        } catch (Exception e) {
            return (float) value;
        }
    }
    public static TemporaryWallet getTempWallet(){
        JsonNode response = makeGetRequest(Urls.TEMPORARY_WALLET_ADDRESS_GENERATOR);
        if(response==null) return null;
        String walletAddress = String.valueOf(response.get("result").get("public"));
        String privateKey = String.valueOf(response.get("result").get("private"));
        return new TemporaryWallet(privateKey.substring(1, privateKey.length() - 1),walletAddress.substring(1, walletAddress.length() - 1));
    }

    public static JsonNode isWalletForwarded(String walletAddress){
        JsonNode response = makeGetRequest(BaseUrl_For_WalletForwarding +"/walletInfo/"+walletAddress);
        try{
            return response;
        }catch (Exception e){
            Log.i("request_error",e.toString());
            return null;
        }
    }

    public static String getRealWalletAddressByActivationCode(String activationCode){
        Log.i("4_digit_code",activationCode);
        JsonNode response = makeGetRequest(Urls.GET_REAL_WALLET_ADDRESS_BY_4_DIGIT_CODE+activationCode);
        Log.i("4_digit_code",response.toString());
        if(response.get("wallet_address")!=null) {
            return response.get("wallet_address").toString();
        }
        Log.d("responseForCodeActivation",response.toString());
        return response.get("error").toString();
    }


    public static String getOnChainBalance(String walletAddress){
        try {
            Log.i("walletAddress_getOnChainBalance", walletAddress);
            JsonNode response = makeGetRequest(Urls.GET_EAT_BALANCE + walletAddress);
            Log.i("response_getOnChainBalance", response + "");
            if (response != null)
                return trimStartingAndEndingCommas(String.valueOf(response.get("balance")));
            else return null;
        }catch (Exception e){
            Log.i("response_getOnChainBalance",e.getMessage());
            return null;
        }
    }

    public static String getBaseUrl_For_WalletForwarding() {
        return BaseUrl_For_WalletForwarding;
    }
    public static String getBaseUrl_For_FourDigitCodeProcessing() {
        return BaseUrl_For_FourDigitCodeProcessing;
    }

    public static boolean isJSONObjectValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public static String trimStartingAndEndingCommas(String str){
        return str.substring(1,str.length()-1);
    }
    public static boolean isValidNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            try {
                Float.parseFloat(str);
                return true;
            } catch (NumberFormatException f) {
                try {
                    Long.parseLong(str);
                    return true;
                } catch (NumberFormatException g) {
                    try {
                        Double.parseDouble(str);
                        return true;
                    } catch (NumberFormatException h) {
                        return false;
                    }
                }
            }
        }
    }
}
