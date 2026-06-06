package com.academicpulse;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EventDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String title = getIntent().getStringExtra("EVENT_TITLE");
        if (title == null) title = "ឈ្មោះព្រឹត្តិការណ៍";
        
        String desc = getIntent().getStringExtra("EVENT_DESC");
        if (desc == null) desc = "មិនមានការពិពណ៌នាអំពីព្រឹត្តិការណ៍";
        
        String date = getIntent().getStringExtra("EVENT_DATE");
        if (date == null) date = "កាលបរិច្ឆេទ";
        
        String time = getIntent().getStringExtra("EVENT_TIME");
        if (time == null) time = "ម៉ោង";
        
        String location = getIntent().getStringExtra("EVENT_LOCATION");
        if (location == null) location = "ទីតាំង";
        
        String imageUrl = getIntent().getStringExtra("EVENT_IMAGE");

        ((TextView) findViewById(R.id.tv_title)).setText(title);
        ((TextView) findViewById(R.id.tv_description)).setText(desc);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                ((ImageView) findViewById(R.id.iv_header)).setImageURI(Uri.parse(imageUrl));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.iv_header)).setImageResource(R.drawable.ic_launcher_background);
                e.printStackTrace();
            }
        }

        setupInfoRow(findViewById(R.id.row_date), "កាលបរិច្ឆេទ", date, android.R.drawable.ic_menu_today);
        setupInfoRow(findViewById(R.id.row_time), "ម៉ោង", time, android.R.drawable.ic_menu_recent_history);
        setupInfoRow(findViewById(R.id.row_location), "ទីតាំង", location, android.R.drawable.ic_menu_mylocation);
    }

    private void setupInfoRow(View view, String label, String value, int iconRes) {
        ((TextView) view.findViewById(R.id.tv_row_label)).setText(label);
        ((TextView) view.findViewById(R.id.tv_row_value)).setText(value);
        ((ImageView) view.findViewById(R.id.iv_row_icon)).setImageResource(iconRes);
    }
}
