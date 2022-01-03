package com.example.nfc_handler;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {
    private Toast toast;
    private TextView textView;
    private PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=(TextView)findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter ==null){
            toast=Toast.makeText(this,"Unsupported NFC.",Toast.LENGTH_LONG);
        }
        if(!nfcAdapter.isEnabled()){
            toast=Toast.makeText(this,"Disabled NFC.",Toast.LENGTH_LONG);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        textView.setText("Null");
        System.out.println(intent.getAction());
        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                ||NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                ||NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            processIntent(intent);
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        System.out.println(getIntent().getAction());
    }

    private String byteToHexString(byte[] src){
        StringBuilder stringBuilder=new StringBuilder("0x");
        if(src==null || src.length<=0){
            return null;
        }
        char[] buffer =new char[2];
        for (byte b : src) {
            buffer[0] = Character.forDigit((b >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(b & 0x0F, 16);
            //System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private void processIntent(Intent intent){
        Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        boolean auth;
        MifareClassic mfc=MifareClassic.get(tag);
        try{
            StringBuilder metaInfo= new StringBuilder();

            mfc.connect();
            int type=mfc.getType();
            int sectorCount=mfc.getSectorCount();

            String typeS="";
            switch(type){
                case  MifareClassic.TYPE_CLASSIC:
                    typeS="TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS="TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS="TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS="TYPE_UNKNOWN";
                    break;
            }
            metaInfo.append("卡片类型：").append(typeS).append("\n共").append(sectorCount).append("个扇区\n共").append(mfc.getBlockCount()).append("个块\n存储空间：").append(mfc.getSize()).append("B\n");
            for (int j=0;j<sectorCount;j++){
                auth=mfc.authenticateSectorWithKeyA(j,MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if(auth){
                    metaInfo.append("Sector").append(j).append("：验证成功\n");

                    bCount=mfc.getBlockCountInSector(j);
                    bIndex=mfc.sectorToBlock(j);
                    for(int i=0;i<bCount;i++){
                        byte[] data=mfc.readBlock(bIndex);
                        metaInfo.append("Block ").append(bIndex).append("：").append(byteToHexString(data)).append("\n");
                        bIndex++;
                    }
                }else{
                    metaInfo.append("Sector").append(j).append("：验证失败\n");
                }
            }
            textView.setText(metaInfo.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}