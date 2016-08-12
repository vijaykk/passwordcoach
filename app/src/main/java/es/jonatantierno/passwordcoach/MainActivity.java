package es.jonatantierno.passwordcoach;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Map;

import es.jonatantierno.passwordcoach.domain.model.dictionary.RxDictionary;
import es.jonatantierno.passwordcoach.domain.model.rules.DictionaryRule;
import es.jonatantierno.passwordcoach.domain.model.rules.ObservableDictionaryRule;
import es.jonatantierno.passwordcoach.domain.model.rules.PasswordMeterRule;
import es.jonatantierno.passwordcoach.domain.model.rules.Result;
import es.jonatantierno.passwordcoach.domain.model.rules.ResultCode;
import es.jonatantierno.passwordcoach.domain.model.rules.SetOfRules;
import es.jonatantierno.passwordcoach.domain.model.rules.ShortPasswordRule;
import es.jonatantierno.passwordcoach.domain.model.rules.ToggableRule;
import es.jonatantierno.passwordcoach.domain.model.tips.TipSource;
import es.jonatantierno.passwordcoach.domain.ports.Gui;
import es.jonatantierno.passwordcoach.infrastructure.AndroidAnalysis;
import es.jonatantierno.passwordcoach.infrastructure.ConfiguredTipSource;
import es.jonatantierno.passwordcoach.infrastructure.KeyboardControl;
import es.jonatantierno.passwordcoach.infrastructure.ObservableTweets;
import es.jonatantierno.passwordcoach.infrastructure.PersistentBoolean;
import es.jonatantierno.passwordcoach.infrastructure.ResultCodeToStringIdMap;
import es.jonatantierno.passwordcoach.infrastructure.repositories.TipFrame;
import es.jonatantierno.passwordcoach.infrastructure.repositories.ZxcvbnPasswordMeter;

public class MainActivity extends AppCompatActivity implements Gui {

    public static final int MIN_LENGTH = 9;
    public static final int MIN_STRENGTH = 3;

    private EditText password;
    private TextView result;
    private View progress;
    private Map<ResultCode, Integer> codeToStringId = new ResultCodeToStringIdMap();

    private TipFrame tipframe;
    private TipSource tipSource;
    private boolean readyToLeave = true;
    private KeyboardControl keyboardControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        progress = findViewById(R.id.progress);
        tipframe = new TipFrame((ViewGroup) findViewById(R.id.tipLayout));


        password = (EditText) findViewById(R.id.passwordEditText);
        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                readyToLeave = false;
                keyboardControl.hide();
                progress.setVisibility(View.VISIBLE);

                new AndroidAnalysis(
                        this,
                        new SetOfRules(
                                Arrays.asList(
                                        new ToggableRule(
                                                new PersistentBoolean(this).load(),
                                                new ObservableDictionaryRule(
                                                        new RxDictionary(
                                                                new ObservableTweets(this).go()).asObservable())
                                        ),
                                        new ShortPasswordRule(MIN_LENGTH),
                                        new DictionaryRule(
                                                this.getResources().openRawResource(R.raw.spanish_words)
                                        ),
                                        new DictionaryRule(
                                                this.getResources().openRawResource(R.raw.common_passwords)
                                        ),
                                        new PasswordMeterRule(new ZxcvbnPasswordMeter(), MIN_STRENGTH)
                                )
                        )
                ).start(password.getText().toString().trim());

                return true;
            } else {
                return false;
            }
        });
        keyboardControl = new KeyboardControl(password, this);

        result = (TextView) findViewById(R.id.resultTextView);

    }

    @Override
    protected void onResume() {
        super.onResume();

        tipSource = new ConfiguredTipSource(this);
    }

    @Override
    public void show(Result analysisResult) {
        result.setText(codeToStringId.get(analysisResult.code()));
        colorizeResult(analysisResult.passwordIsStrong());

        tipframe.show(tipSource.tip(analysisResult));
        progress.setVisibility(View.GONE);
    }

    private void colorizeResult(boolean isStrong) {
        int color = R.color.result_weak;
        if (isStrong) {
            color = R.color.colorPrimaryDark;
        }
        result.setTextColor(this.getResources().getColor(color));
    }

    @Override
    public void onBackPressed() {
        if (readyToLeave) {
            super.onBackPressed();
        } else {
            readyToLeave = true;
            tipframe.hide();
            password.setText("");
            keyboardControl.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void goToDictionaryActivity(MenuItem item) {
        startActivity(new Intent(this, DictionaryActivity.class));
    }
}
