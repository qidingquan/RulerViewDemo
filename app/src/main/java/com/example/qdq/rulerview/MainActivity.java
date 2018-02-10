package com.example.qdq.rulerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    RulerView view_rule;
    TextView tv_num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view_rule=findViewById(R.id.view_rule);
        tv_num=findViewById(R.id.tv_num);

        view_rule.setOnValueChangeListener(new RulerView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {
                tv_num.setText(value+"");
            }
        });
    }
}
