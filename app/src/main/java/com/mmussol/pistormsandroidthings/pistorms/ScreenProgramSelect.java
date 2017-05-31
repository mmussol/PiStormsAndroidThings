package com.mmussol.pistormsandroidthings.pistorms;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.mmussol.pistormsandroidthings.R;

import static com.mmussol.pistormsandroidthings.pistorms.Constants.PS_TAG;

class ScreenProgramSelect extends Presentation {
    public ScreenProgramSelect(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ps_program_select);
        Log.d(PS_TAG, "TestPresentation setContentView Done");

        Button b1 = (Button) findViewById(R.id.test_button_1);
        Button b2 = (Button) findViewById(R.id.test_button_2);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(PS_TAG, "VIRTUAL DISPLAY BUTTON 1 pressed!!");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(PS_TAG, "VIRTUAL DISPLAY BUTTON 2 pressed!!");
            }
        });
    }
}
