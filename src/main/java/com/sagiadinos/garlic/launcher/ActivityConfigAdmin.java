/*
 garlic-launcher: Android Launcher for the Digital Signage Software garlic-player

 Copyright (C) 2020 Nikolaos Sagiadinos <ns@smil-control.com>
 This file is part of the garlic-launcher source code

 This program is free software: you can redistribute it and/or  modify
 it under the terms of the GNU Affero General Public License, version 3,
 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sagiadinos.garlic.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.sagiadinos.garlic.launcher.configuration.SharedPreferencesModel;
import com.sagiadinos.garlic.launcher.helper.AppPermissions;
import com.sagiadinos.garlic.launcher.helper.GarlicLauncherException;
import com.sagiadinos.garlic.launcher.configuration.PasswordHasher;
import com.sagiadinos.garlic.launcher.configuration.MainConfiguration;
import com.sagiadinos.garlic.launcher.services.HUD;

import java.util.Objects;

public class ActivityConfigAdmin extends Activity
{
    TextView tvInformation;
    NumberPicker editPlayerStartDelay;
    CheckBox cbOwnBackButton;
    CheckBox cbNoPlayerStartDelayAfterBoot;
    CheckBox cbActiveServicePassword;
    EditText editServicePassword;
    Boolean  is_password_changed = false;
    MainConfiguration MyMainConfiguration;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_config_admin);
        cbOwnBackButton          = findViewById(R.id.cbOwnBackButton);
        cbActiveServicePassword  = findViewById(R.id.cbActiveServicePassword);
        editServicePassword      = findViewById(R.id.editServicePassword);
        tvInformation            = findViewById(R.id.textViewInformation);
        MyMainConfiguration      = new MainConfiguration(new SharedPreferencesModel(this));

        editPlayerStartDelay = findViewById(R.id.editPlayerStartDelay);
        editPlayerStartDelay.setMaxValue(99);
        editPlayerStartDelay.setMinValue(5);
        editPlayerStartDelay.setValue(MyMainConfiguration.getPlayerStartDelay());
        cbNoPlayerStartDelayAfterBoot = findViewById(R.id.cbNoPlayerStartDelayAfterBoot);
        cbNoPlayerStartDelayAfterBoot.setChecked(MyMainConfiguration.hasNoPlayerStartDelayAfterBoot());
        prepareOptionsVisibility();
    }

    public void saveAndClose(View view)
    {
        hideErrorText();

        try
        {
            checkServicePassword();
            toggleOwnBackButton();
            storeNewPlayerStartDelay();
            MyMainConfiguration.toggleNoPlayerStartDelayAfterBoot(cbNoPlayerStartDelayAfterBoot.isChecked());
            finish();

        }
        catch (GarlicLauncherException e)
        {
            displayErrorText(Objects.requireNonNull(e.getMessage()));
        }

    }

    public void closeActivity(View view)
    {
        finish();
    }

    private void toggleOwnBackButton()
    {
        MyMainConfiguration.toggleOwnBackButton(cbOwnBackButton.isChecked());
        if (cbOwnBackButton.isChecked())
        {
            startService(new Intent(this, HUD.class));
        }
        else
        {
            stopService(new Intent(this, HUD.class));
        }
    }

    public void onServicePassWordClicked(View view)
    {
        prepareVisibilityOfEditServicePassword(cbActiveServicePassword.isChecked());
    }

    private void storeNewPlayerStartDelay()
    {
        int count = editPlayerStartDelay.getValue();

        if (count < 5)
        {
            count = 5;
        }
        MyMainConfiguration.storePlayerStartDelay(count);
    }

    private void prepareOptionsVisibility()
    {
        prepareVisibilityOfBackButtonOption();
        prepareVisibilityOfServicePasswordOption();

    }

    private void prepareVisibilityOfBackButtonOption()
    {
        if (MyMainConfiguration.isDeviceRooted() && AppPermissions.verifyOverlayPermissions(this))
        {
            cbOwnBackButton.setVisibility(View.VISIBLE);
            cbOwnBackButton.setEnabled(true);
            cbOwnBackButton.setChecked(MyMainConfiguration.hasOwnBackButton());

        }
        else
        {
            cbOwnBackButton.setEnabled(false);
            cbOwnBackButton.setVisibility(View.GONE);
        }
    }

    private void prepareVisibilityOfServicePasswordOption()
    {
        cbActiveServicePassword.setChecked(MyMainConfiguration.hasActiveServicePassword());

        prepareVisibilityOfEditServicePassword(MyMainConfiguration.hasActiveServicePassword());
    }

    public void prepareVisibilityOfEditServicePassword(Boolean is_checked)
    {
        if (is_checked)
        {
            editServicePassword.setVisibility(View.VISIBLE);
            editServicePassword.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s){}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    is_password_changed = true;
                }
            });
        }
        else
        {
            editServicePassword.setVisibility(View.GONE);
        }

    }

    private void checkServicePassword() throws GarlicLauncherException
    {
        String password = editServicePassword.getText().toString();

        if (cbActiveServicePassword.isChecked() && is_password_changed && password.isEmpty())
        {
            throw new GarlicLauncherException(getString(R.string.missing_password));
        }

        MyMainConfiguration.toggleActiveServicePassword(cbActiveServicePassword.isChecked());
        if (is_password_changed)
        {
            MyMainConfiguration.setServicePassword(password, new PasswordHasher());
        }
    }

    private void displayErrorText(String error_text)
    {
        tvInformation.setText(error_text);
        tvInformation.setVisibility(View.VISIBLE);
    }

    private void hideErrorText()
    {
        tvInformation.setText("");
        tvInformation.setVisibility(View.INVISIBLE);
    }

}
