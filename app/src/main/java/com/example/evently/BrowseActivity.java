package com.example.evently;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class BrowseActivity extends AppCompatActivity {

    private LinearLayout listContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        listContainer = findViewById(R.id.listContainer);

        List<EventListItem> events = seedEvents();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (EventListItem e : events) {
            View item = inflater.inflate(R.layout.item_event, listContainer, false);

            ImageView poster = item.findViewById(R.id.imgPoster);
            TextView title = item.findViewById(R.id.txtTitle);
            TextView status = item.findViewById(R.id.txtStatus);
            TextView selectionDate = item.findViewById(R.id.txtselectionDate);
            TextView eventDate = item.findViewById(R.id.txtDate);
            Button details = item.findViewById(R.id.btnDetails);

            poster.setImageResource(e.posterResId);
            title.setText(e.title);
            status.setText(e.status);

            if (e.selectionDate == null || e.selectionDate.isEmpty()) {
                selectionDate.setVisibility(View.GONE);
            } else {
                selectionDate.setVisibility(View.VISIBLE);
                selectionDate.setText("• " + e.selectionDate);
            }

            eventDate.setText(e.eventDate);

            listContainer.addView(item);
        }
    }

    private List<EventListItem> seedEvents() {
        List<EventListItem> items = new ArrayList<>();
        int poster = android.R.drawable.ic_menu_report_image; // placeholder poster

        items.add(new EventListItem("Whale Watching",
                "Confirmed", null, "2026-02-14", poster));

        items.add(new EventListItem("LAN Gaming",
                "Closed", "Selection on 2026-02-25", "2026-03-09", poster));

        items.add(new EventListItem("Spelling Bee",
                "Open", "Selection on 2026-02-21", "2026-03-01", poster));

        items.add(new EventListItem("Dog Park",
                "Open", "Selection on 2025-12-01", "2025-12-09", poster));

        items.add(new EventListItem("Cat Cafe",
                "Closed", "Selection on 2026-02-05", "2026-02-20", poster));

        return items;
    }
}
