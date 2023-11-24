package com.app.nao.photorecon.ui.license;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.app.nao.photorecon.R;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
public class LicenseViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_category);

        TextView textGeneral = findViewById(R.id.textGeneral);
        TextView textOthers = findViewById(R.id.textOthers);

        textGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generalがクリックされたときの処理
                startActivity(new Intent(v.getContext(), OssLicensesMenuActivity.class));
            }
        });

        textOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Othersがクリックされたときの処理
            }
        });
    }
}
