package org.jak_linux.dns66.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jak_linux.dns66.Configuration;
import org.jak_linux.dns66.FileHelper;
import org.jak_linux.dns66.MainActivity;
import org.jak_linux.dns66.R;
import org.jak_linux.dns66.db.RuleDatabase;

import java.util.ArrayList;
import java.util.List;

public class LogFragment extends Fragment // implements FloatingActionButtonFragment
 {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefresh;

    public LogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.log_entries);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshAdapter();
                    }
                }
        );
        swipeRefresh.setRefreshing(true);

        refreshAdapter();

        Switch hostEnabled = (Switch) rootView.findViewById(R.id.log_enabled);
        hostEnabled.setChecked(MainActivity.config.logs.enabled);
        hostEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.config.logs.enabled = isChecked;
                FileHelper.writeSettings(getContext(), MainActivity.config);
            }
        });

        Switch onlyBlocked = (Switch) rootView.findViewById(R.id.switch_onlyblocked);
        onlyBlocked.setChecked(MainActivity.config.logs.showOnlyBlocked);
        onlyBlocked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.config.logs.showOnlyBlocked = isChecked;
                FileHelper.writeSettings(getContext(), MainActivity.config);
                refreshAdapter();
            }
        });


        ExtraBar.setup(rootView.findViewById(R.id.extra_bar), "log");

        return rootView;
    }

    private void refreshAdapter() {
        //mAdapter = new LogAdapter(LogDatabase.getInstance().getEntries());
        List<Configuration.Item> items = null;
        if (MainActivity.config.logs.showOnlyBlocked) {
            items = new ArrayList<>();
            for (Configuration.Item item: MainActivity.config.logs.items) {
                if (item.state == 1) {
                    items.add(item);
                }
            }
        } else {
            items = MainActivity.config.logs.items;
        }
        RecyclerView.Adapter mAdapter = new ItemRecyclerViewAdapter(items, 2, new ItemRecyclerViewAdapter.StateListener() {
            @Override
            public void stateChanged(Configuration.Item item) {
                if (!MainActivity.config.logs.keepItems.contains(item)) {
                    MainActivity.config.logs.keepItems.add(item);
                }
                if (item.state == 1) {
                    RuleDatabase.getInstance().personalBlock(item.location);
                } else {
                    RuleDatabase.getInstance().personalUnblock(item.location);
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        swipeRefresh.setRefreshing(false);
    }
/*
    public void setupFloatingActionButton(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //do what?
            }
        });
    }
*/
}
