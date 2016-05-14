package com.techlo.easypark;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ExitInvoice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit_invoice);

        TextView tamount= (TextView) findViewById(R.id.t_amountpayable_exitinvoie);
        TextView tTotalTime= (TextView) findViewById(R.id.t_totaltime_exitinvoie);
        TextView tEnteredAt= (TextView) findViewById(R.id.t_enteredat_exitinvoie);
        TextView tExitAt= (TextView) findViewById(R.id.t_exitat_exitinvoie);
        TextView tVehiclNo= (TextView) findViewById(R.id.t_vehicleno);

        Intent intent = getIntent();

        String amount = intent.getStringExtra(Fields.AMOUNT);
        String totalTime = intent.getStringExtra(Fields.TOTAL_TIME);
        String enteredAt = intent.getStringExtra(Fields.ENTERED_AT);
        String exitAt = intent.getStringExtra(Fields.EXIT_AT);
        String vehicleNo = intent.getStringExtra(Fields.VEHICLE_NO);


        tamount.setText(amount);
        tTotalTime.setText(totalTime);
        tEnteredAt.setText(enteredAt);
        tExitAt.setText(exitAt);
        tVehiclNo.setText(vehicleNo);


    }

    public void printinvoice(View view) {
        Toast.makeText(getApplicationContext(),"This feature is coming soon",Toast.LENGTH_SHORT).show();
    }
}
