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
package com.sagiadinos.garlic.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sagiadinos.garlic.launcher.BuildConfig;
import com.sagiadinos.garlic.launcher.configuration.SharedPreferencesModel;
import com.sagiadinos.garlic.launcher.helper.DeviceOwner;
import com.sagiadinos.garlic.launcher.configuration.MainConfiguration;

import java.util.Objects;

public class InstalledAppReceiver extends BroadcastReceiver
{

    MainConfiguration MyMainConfiguration;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent == null || intent.getAction() == null)
        {
            return;
        }

        String s = Objects.requireNonNull(intent.getData()).toString();
        if (s.equals("package:com.sagiadinos.garlic.launcher"))
        {
            return;
        }
        MyMainConfiguration = new MainConfiguration(new SharedPreferencesModel(context));

        // On an Update all Actions (REMOVE, ADD and REPLACE)  are triggered.
        // So we must do some preventations, cause it reboot after REMOVE
        // And the player disappear and must be download again

        // Solution:
        // Check the extras for for Replacing key and in this case ignore REMOVE and ADDED
        if (intent.getExtras().containsKey(Intent.EXTRA_REPLACING) &&
                (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
                        intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)
                )
        )
        {
            return;
        }

        if (s.equals("package:"+ DeviceOwner.PLAYER_PACKAGE_NAME))
        {
            tooglePlayerInstalled(isPlayerInstalled(intent));
        }

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)
            )
        {
            if (!BuildConfig.DEBUG)
            {
                Intent i = new Intent("com.sagiadinos.garlic.launcher.receiver.CommandReceiver");
                i.putExtra("command", "reboot");
                context.sendBroadcast(new Intent("com.sagiadinos.garlic.launcher.receiver.CommandReceiver"));
            }
        }
    }

    private Boolean isPlayerInstalled(Intent intent)
    {
        return !Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_PACKAGE_REMOVED);
    }

    void tooglePlayerInstalled(Boolean installed)
    {
        MyMainConfiguration.togglePlayerInstalled(installed);
    }

}
