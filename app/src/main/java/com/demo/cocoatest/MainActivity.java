package com.demo.cocoatest;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.square.MagRead;
import com.square.MagReadListener;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    Context context;
    private MagRead read;
    private UpdateBytesHandler updateBytesHandler;
    public final String API_KEY = "pk_test_MStXBAw447efpVXwO2EkcxPo";
    final String STRIPE_API="https://cocoapay.herokuapp.com/api/stripe/payment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        read = new MagRead();

        read.addListener(new MagReadListener() {

            @Override
            public void updateBytes(String bytes) {
                Message msg = new Message();
                msg.obj = bytes;
                updateBytesHandler.sendMessage(msg);
            }

            @Override
            public void updateBits(String bits) {
                Message msg = new Message();
                msg.obj = bits;
                //updateBitsHandler.sendMessage(msg);

            }
        });
        updateBytesHandler = new UpdateBytesHandler();


        ((Button)findViewById(R.id.pay)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount=((EditText)findViewById(R.id.amount)).getText().toString().trim();

                if(amount.isEmpty()){
                    Toast.makeText(context,"Please enter amount",Toast.LENGTH_SHORT).show();
                }else{
                    paymentPopup(amount);
                }

            }
        });

    }



    private class UpdateBytesHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            String bytes = (String) msg.obj;

            try{
                Log.e("bytes",""+bytes);
            }catch (Exception e){e.printStackTrace();}

//            location.setText(bytes);
//			String bytes=";4029850306302594=230111260000059100000?";
            Log.d("lenght of byte is :",""+bytes.length());

            try {
                if (bytes.length() > 22) {

//			Log.e("card no",bytes.substring(bytes.indexOf(";")+1,bytes.indexOf("=")));

                    if (bytes.substring(bytes.indexOf(";") + 1, bytes.indexOf("=")).length() < 16) {
//				Toast.makeText(appContext,"Please swipe again! ",Toast.LENGTH_LONG).show();
                    } else if (bytes.substring(bytes.indexOf(";") + 1, bytes.indexOf("=")).length() > 16) {
//				Toast.makeText(appContext,"Please swipe again! ",Toast.LENGTH_LONG).show();
                    } else if (bytes.substring(bytes.indexOf("=") + 1, bytes.length() - 1).length() < 4) {
//				Toast.makeText(appContext,"Please swipe again! ",Toast.LENGTH_LONG).show();
                    } else {

                        String temp = bytes.substring(1);
                        String cardno = temp.substring(0, Math.min(bytes.length(), 16));
                        String exp_year = bytes.substring(18, 20);
                        String exp_month = bytes.substring(20, 22);
                        card_holder_no.setText(cardno);
                        exp_date.setText(exp_month + "/" + exp_year);

                    }
                }
            }catch (Exception e){e.printStackTrace();}

            //decodedStringView.setText(bytes);

        }

    }

    EditText card_holder_no;
    EditText exp_date;
    EditText card_cvv;


    Dialog dialog;
    String mLastInput ="";
    public void paymentPopup(final String charge_amount) {

        // TODO Auto-generated method stub
        // custom dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(-2,-2);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	/*	Log.d("card no is :", "" + cardno + " and expiry year  is :" + exp_year + " and expiry month is:" + exp_month);
*/

       // read.start();
        dialog.setContentView(R.layout.payment_popup);
        dialog.show();

        RelativeLayout viewLayout = (RelativeLayout) dialog
                .findViewById(R.id.rl2);
        ImageView closeBtn = (ImageView) dialog.findViewById(R.id.close_btn);
        ImageView nxtImageView = (ImageView) dialog
                .findViewById(R.id.nxt_arrow_iv);


        ((EditText) dialog.findViewById(R.id.card_holdername)).setVisibility(View.GONE);
        ((TextView)dialog.findViewById(R.id.price_score)).setText("₹ "+charge_amount);
        nxtImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                paymentPopupCart();
            }
        });

        viewLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //paymentPopupCart();
            }
        });

        card_holder_no = (EditText)dialog.findViewById(R.id.cardHolderNumber);
        exp_date =(EditText)dialog.findViewById(R.id.cardExpiry);
        card_cvv =(EditText)dialog.findViewById(R.id.cardCvv);

        closeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (dialog != null && dialog.isShowing()) {
                    read.stop();
                    dialog.cancel();
                }
            }
        });
        exp_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                SimpleDateFormat formatter = new SimpleDateFormat("MM/yy", Locale.GERMANY);
                Calendar expiryDateDate = Calendar.getInstance();
                try {
                    expiryDateDate.setTime(formatter.parse(input));
                } catch (ParseException e) {
                    if (s.length() == 2 && !mLastInput.endsWith("/")) {
                        int month = Integer.parseInt(input);
                        if (month <= 12) {
                            exp_date.setText(exp_date.getText().toString() + "/");
                            exp_date.setSelection(exp_date.getText().toString().length());
                        }
                    }else if (s.length() == 2 && mLastInput.endsWith("/")) {
                        int month = Integer.parseInt(input);
                        if (month <= 12) {
                            exp_date.setText(exp_date.getText().toString().substring(0,1));
                            exp_date.setSelection(exp_date.getText().toString().length());
                        } else {
                            exp_date.setText("");
                            exp_date.setSelection(exp_date.getText().toString().length());
                            Toast.makeText(getApplicationContext(), "Enter a valid month", Toast.LENGTH_LONG).show();
                        }
                    } else if (s.length() == 1){
                        int month = Integer.parseInt(input);
                        if (month > 1) {
                            exp_date.setText("0" + exp_date.getText().toString() + "/");
                            exp_date.setSelection(exp_date.getText().toString().length());
                        }
                    }
                    else {

                    }
                    mLastInput = exp_date.getText().toString();
                    return;

                }
            }
        });



        ((Button) dialog.findViewById(R.id.payment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(!card_holder_no.getText().toString().isEmpty()&&
                        !card_holder_no.getText().toString().isEmpty()&&
                !card_holder_no.getText().toString().isEmpty()) {


                    String dateString = exp_date.getText().toString();
                    int month = Integer.parseInt(dateString.substring(0,2));
                    int year = Integer.parseInt(dateString.substring(3));

                    Card card = new Card(card_holder_no.getText().toString().trim(),
                                        month,
                                        year,
                                        card_cvv.getText().toString());

                    Log.d("CARD:",card.getCVC()+" "+card.getExpMonth()+" "+card.getExpYear()+" "+card.getNumber());


                    try {
                        processPayment(card,charge_amount);
                    } catch (AuthenticationException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();

                    /*

                    VALIDATING CARD
                    if(card.validateCard()){

                    }else{
                        Toast.makeText(getApplicationContext(),"Invalid card details!",Toast.LENGTH_SHORT).show();

                    }
                    */
                }
            }
        });
    }


    public void processPayment(Card card,final String amount) throws AuthenticationException {
        Stripe stripe = new Stripe(API_KEY);
        stripe.createToken(
                card,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        Log.d("Token:",token.getId());
                           //Hit the api with the token and payment type details
                        new chargeUser().execute(token.getId(),amount);



                    }
                    public void onError(Exception error) {
                        // Show localized error message
                        Log.d("Err:",error.getLocalizedMessage().toString());
                        Toast.makeText(getApplicationContext(),
                                "Error! Occured!",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    public class chargeUser extends AsyncTask<String,String,String>{
        String result;
        int code;
        RestClient restClient = new RestClient(STRIPE_API);

        @Override
        protected void onPreExecute() {


            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... param) {
            Log.d("amount", param[1]);
            Log.d("source", param[0]);
            restClient.addParam("amount", param[1]);
            restClient.addParam("currency", "INR");
            restClient.addParam("description", "Example charge");
            restClient.addParam("token", param[0]);

            try {
                restClient.executePost();
                result = restClient.getResponse();
                code = restClient.getCode();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Res",result+code);
            super.onPostExecute(result);
        }
    }
}
