package org.ligi.vaporizercontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import org.ligi.vaporizercontrol.util.TemperatureFormatter;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class DataDisplayActivity extends AppCompatActivity implements VaporizerData.VaporizerUpdateListener {

    @InjectView(R.id.progress_indicator)
    View progress_indicator;

    @InjectView(R.id.intro_text)
    TextView introText;

    @OnClick(R.id.led)
    void ledClick() {
        final boolean isUnknownOrNotBright = getApp().getVaporizerCommunicator().getData().ledPercentage == null ||
                                             getApp().getVaporizerCommunicator().getData().ledPercentage == 0;
        getApp().getVaporizerCommunicator().setLEDBrightness(this, isUnknownOrNotBright ? 100 : 0);
    }


    @InjectView(R.id.battery)
    TextView battery;

    @InjectView(R.id.temperature)
    TextView temperature;

    @InjectView(R.id.temperatureSetPoint)
    TextView temperatureSetPoint;

    @InjectView(R.id.tempBoost)
    TextView tempBoost;

    @InjectView(R.id.led)
    TextView led;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    protected void onPause() {
        getApp().getVaporizerCommunicator().destroy();
        super.onPause();
    }

    @Override
    protected void onResume() {
        getApp().getVaporizerCommunicator().connectAndRegisterForUpdates(this);
        onUpdate(getApp().getVaporizerCommunicator().getData());
        super.onResume();
    }

    private App getApp() {
        return (App) getApplication();
    }

    @Override
    public void onUpdate(final VaporizerData data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (introText.getVisibility() == VISIBLE) {
                    if (data.hasData()) {
                        introText.setVisibility(GONE);
                        progress_indicator.setVisibility(GONE);
                    } else {
                        introText.setText(Html.fromHtml(getString(R.string.intro_text)));
                        introText.setMovementMethod(new LinkMovementMethod());
                    }
                }

                battery.setText((data.batteryPercentage == null ? "?" : "" + data.batteryPercentage) + "%");
                temperature.setText(getFormattedTemp(data.currentTemperature) + " / ");
                temperatureSetPoint.setText(getFormattedTemp(data.setTemperature));
                tempBoost.setText("+" + getFormattedTemp(data.boostTemperature));
                led.setText((data.ledPercentage == null ? "?" : "" + data.ledPercentage) + "%");
            }
        });
    }

    private String getFormattedTemp(Integer temp) {
        return TemperatureFormatter.getFormattedTemp(temp, getApp().getSettings().getTemperatureFormat());
    }
}
