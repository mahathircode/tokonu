package nu.toko.Page;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nu.toko.Adapter.BillingAdapter;
import nu.toko.Adapter.BillingItemAdapter;
import nu.toko.Adapter.PayListAdapter;
import nu.toko.MainActivity;
import nu.toko.Model.BillingItemModel;
import nu.toko.Model.BillingModelNU;
import nu.toko.Model.ProductModel;
import nu.toko.Model.ProductModelNU;
import nu.toko.R;
import nu.toko.Reqs.ReqString;
import nu.toko.Utils.Others;

import static nu.toko.Utils.Staticvar.BAYAR;
import static nu.toko.Utils.Staticvar.GAMBARADD;
import static nu.toko.Utils.Staticvar.GAMBARFIRST;
import static nu.toko.Utils.Staticvar.HARGA_ADMIN;
import static nu.toko.Utils.Staticvar.HARGA_MITRA;
import static nu.toko.Utils.Staticvar.HARGA_ONGKIR;
import static nu.toko.Utils.Staticvar.HARGA_TOTAL;
import static nu.toko.Utils.Staticvar.ID_MITRA;
import static nu.toko.Utils.Staticvar.ID_PEMBELI;
import static nu.toko.Utils.Staticvar.ID_PRODUK;
import static nu.toko.Utils.Staticvar.ID_TRANSAKSI;
import static nu.toko.Utils.Staticvar.ID_TRANSAKSI_ITEM;
import static nu.toko.Utils.Staticvar.NAMA_PRODUK;
import static nu.toko.Utils.Staticvar.PRODUK;
import static nu.toko.Utils.Staticvar.QTY;
import static nu.toko.Utils.Staticvar.RESI;
import static nu.toko.Utils.Staticvar.SUB_TOTAL;
import static nu.toko.Utils.Staticvar.TGL_PEMESANAN;
import static nu.toko.Utils.Staticvar.TRANSAKSIDETAIL;

public class PagePay extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 786;
    String TAG = getClass().getSimpleName();
    List<BillingItemModel> billingItemModels;
    PayListAdapter billadap;
    RecyclerView rvpay;
    RequestQueue requestQueue;
    String idtrans;
    FrameLayout pilihfile;
    ImageView imagetampil;
    CardView cofirm;
    CardView tentukan;
    Bitmap buktitrans = null;
    ProgressBar progres;
    TextView subtotal, ongkir, total, paytex;
    Calendar c;
    String tanggal = "";
    TextView err, namalengkapbank, namabank, norekbank;
    EditText tanggaltransfer, jumlahtransfer, namabanktujuan, namalengkap, norek;

    File file = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_pay);

        idtrans = String.valueOf(getIntent().getIntExtra(ID_TRANSAKSI, 0));
        init();
        new ReqString(this, requestQueue).go(respon, TRANSAKSIDETAIL+idtrans);
    }

    void init(){
        paytex = findViewById(R.id.paytex);
        progres = findViewById(R.id.progres);
        pilihfile = findViewById(R.id.pilihfile);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        rvpay = findViewById(R.id.rvpay);
        billingItemModels = new ArrayList<>();
        billadap = new PayListAdapter(this, billingItemModels);
        rvpay.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvpay.setAdapter(billadap);
        pilihfile.setOnClickListener(new klik());
        imagetampil = findViewById(R.id.imagetampil);
        cofirm = findViewById(R.id.cofirm);
        cofirm.setOnClickListener(new klik());
        subtotal = findViewById(R.id.subtotal);
        ongkir = findViewById(R.id.ongkir);
        total = findViewById(R.id.total);
        tentukan = findViewById(R.id.tentukan);
        err = findViewById(R.id.err);
        namalengkapbank = findViewById(R.id.namalengkapbank);
        namabank = findViewById(R.id.namabank);
        norekbank = findViewById(R.id.norekbank);
        norek = findViewById(R.id.norek);
        tanggaltransfer = findViewById(R.id.tanggaltransfer);
        jumlahtransfer = findViewById(R.id.jumlahtransfer);
        namabanktujuan = findViewById(R.id.namabanktujuan);
        namalengkap = findViewById(R.id.namalengkap);
        tentukan.setOnClickListener(new klik());
        findViewById(R.id.back).setOnClickListener(new klik());
    }

    class klik implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.back:
                    onBackPressed();
                    break;
                case R.id.pilihfile:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                    } else {
                        CropImage.startPickImageActivity(PagePay.this);
                    }
                break;
                case R.id.cofirm:
                    err.setText("");

                    String nama = namalengkap.getText().toString();
                    String bank = namabanktujuan.getText().toString();
                    String nominal = jumlahtransfer.getText().toString();
                    String noo = norek.getText().toString();

                    if (tanggal.isEmpty()){
                        err.setText("Tentukan Tanggal Transfer");
                        return;
                    }

                    if (nama.isEmpty()){
                        err.setText("Isi Nama Lengkap Konfirmasi");
                        return;
                    }

                    if (noo.isEmpty()){
                        err.setText("Isi No Rek Konfirmasi");
                        return;
                    }

                    if (nominal.isEmpty()){
                        err.setText("Isi Nominal Konfirmasi");
                        return;
                    }

                    if (bank.isEmpty()){
                        err.setText("Isi Nama Bank Konfirmasi");
                        return;
                    }

                    if (file == null){
                        err.setText("Tambahkan Bukti Foto Konfirmasi");
                        return;
                    }

                    paytex.setVisibility(View.INVISIBLE);
                    cofirm.setCardBackgroundColor(getResources().getColor(R.color.white));
                    progres.setVisibility(View.VISIBLE);
                    new ReqString(PagePay.this, requestQueue).multipart(responmultipart, BAYAR, idtrans, tanggal, file, nama, noo, bank, nominal);
                break;
                case R.id.tentukan:
                    // Get Current Date
                    c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(PagePay.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    tanggal = tanggal+year + "-" + (monthOfYear + 1) + "-" + dayOfMonth +" ";
                                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                                    int mMinute = c.get(Calendar.MINUTE);
                                    // Launch Time Picker Dialog
                                    TimePickerDialog timePickerDialog = new TimePickerDialog(PagePay.this, new TimePickerDialog.OnTimeSetListener() {
                                                @Override
                                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                    tanggal = tanggal + hourOfDay + ":" + minute+":00";
                                                    tanggaltransfer.setText(tanggal);
                                                }
                                            }, mHour, mMinute, false);
                                    timePickerDialog.show();
                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                    break;
            }
        }
    }

    Response.Listener<String> respon = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.i(TAG, "onResponse: "+response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                BillingModelNU bill = new BillingModelNU();
                bill.setId_transaksi(jsonObject.getInt(ID_TRANSAKSI));
                bill.setId_pembeli(jsonObject.getInt(ID_PEMBELI));
                bill.setSub_total(jsonObject.getInt(SUB_TOTAL));
                bill.setHarga_ongkir(jsonObject.getInt(HARGA_ONGKIR));
                bill.setHarga_total(jsonObject.getInt(HARGA_TOTAL));
                bill.setTgl_pemesanan(jsonObject.getString(TGL_PEMESANAN));
                bill.setResi(jsonObject.getString(RESI));

                JSONObject mitra = jsonObject.getJSONObject("mitra");
                bill.setId_mitra(mitra.getInt(ID_MITRA));
                namabank.setText(": "+mitra.getString("nama_bank"));
                namalengkapbank.setText(": "+mitra.getString("nama_mitra"));
                norekbank.setText(": "+mitra.getString("no_rekening_mitra"));

                subtotal.setText("Rp."+Others.PercantikHarga(jsonObject.getInt(SUB_TOTAL)));
                ongkir.setText("Rp."+Others.PercantikHarga(jsonObject.getInt(HARGA_ONGKIR)));
                total.setText("Rp."+Others.PercantikHarga(jsonObject.getInt(HARGA_TOTAL)));

                //Transaksi Item
                JSONArray item = new JSONArray(jsonObject.getString("item"));
                for (int p = 0; p < item.length(); p++){
                    Log.i(TAG, "onResponse: XXX");

                    JSONObject objekitem = item.getJSONObject(p);
                    BillingItemModel billitem = new BillingItemModel();
                    billitem.setId_transaksi(objekitem.getString(ID_TRANSAKSI));
                    billitem.setId_transaksi_item(objekitem.getString(ID_TRANSAKSI_ITEM));
                    billitem.setQty(objekitem.getInt(QTY));

                    Log.i(TAG, "onResponse: COK");

                    //Produk
                    JSONObject jsonproduk = new JSONObject(objekitem.getString(PRODUK));
                    ProductModelNU produk = new ProductModelNU();
                    produk.setId_produk(jsonproduk.getString(ID_PRODUK));
                    produk.setNama_produk(jsonproduk.getString(NAMA_PRODUK));
                    produk.setGambarfirst(jsonproduk.getString(GAMBARFIRST));
                    produk.setHarga_admin(jsonproduk.getInt(HARGA_ADMIN));
                    produk.setHarga_mitra(jsonproduk.getInt(HARGA_MITRA));

                    Log.i(TAG, "onResponse: RES");

                    billitem.setProduk(produk);

                    billingItemModels.add(billitem);
                }

                billadap.notifyDataSetChanged();
            } catch (JSONException e){
                Log.i(TAG, "extracdata: Err "+e.getMessage());
            }
        }
    };

    private void Ngecorep(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setRequestedSize(500, 500)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CropImage.startPickImageActivity(this);
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                Ngecorep(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imagetampil.setImageURI(result.getUri());
                Log.i(TAG, "onActivityResult: uri "+result.getUri());
                findViewById(R.id.ikonupload).setVisibility(View.GONE);
                String path = result.getUri().toString();
                try {
                    Bitmap foto = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    buktitrans = foto;

                    file = new File(new URI(path));
                } catch (Exception e){
                    Log.i("Catch", e.getMessage());
                }
            }
        }

    }

    Response.Listener<String> responmultipart = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.i(TAG, "onResponse: Sukses");
            finish();
        }
    };

}
