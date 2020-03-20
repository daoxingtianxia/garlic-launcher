package com.sagiadinos.garlic.launcher.helper.configxml;

import android.os.Environment;
import android.util.Log;

import com.sagiadinos.garlic.launcher.helper.SharedConfiguration;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Objects;

public class ConfigXMLModel
{
    private String smil_index_url   = "http://indexes.smil-control.com";
    private NetworkData MyNetworkData;
    private SharedConfiguration MySharedConfiguration;

    public ConfigXMLModel(NetworkData myNetworkData, SharedConfiguration mySharedConfiguration)
    {
        this.MyNetworkData        = myNetworkData;
        this.MySharedConfiguration = mySharedConfiguration;
    }

    public boolean storeSmilIndexUrl(String smil_index_url)
    {
        this.smil_index_url = smil_index_url;
        return MySharedConfiguration.writeSmilIndex(smil_index_url);
    }

    public String readConfigXml(File config_xml) throws IOException
    {
        BufferedReader reader        = new BufferedReader(new FileReader(config_xml));
        StringBuilder  stringBuilder = new StringBuilder();
        char[]         buffer        = new char[10];
        while (reader.read(buffer) != -1)
        {
            stringBuilder.append(new String(buffer));
            buffer = new char[10];
        }
        reader.close();
        return stringBuilder.toString();
    }

    public void parseConfigXml(String xml)
    {
        try
        {
            XmlPullParser xpp = createXmlPullParser();
            xpp.setInput(new StringReader(xml));

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("prop"))
                {
                    parseProp(xpp);
                }
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException | IOException e)
        {
            Log.e("config_xml", Objects.requireNonNull(e.getMessage()));
        }

    }

    public void copy(File src, File dst) throws IOException
    {
        try (InputStream in = new FileInputStream(src))
        {
            try (OutputStream out = new FileOutputStream(dst))
            {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public void storeConfigXmlForPlayer()
    {
        try
        {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/config.xml");

            if (!file.exists() && !file.createNewFile())
            {
                throw new IOException("File could not be created");
            }
            FileWriter writer = new FileWriter(file);
            writer.write(generateConfigXML());
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            Log.e("config_xml", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void parseProp(XmlPullParser xpp)
    {
        switch (xpp.getAttributeValue(null, "name"))
        {
            case "net.wifi.ssid":
                MyNetworkData.setWifiSSID(xpp.getAttributeValue(null, "value"));
                break;
            case "net.wifi.authentication":
                MyNetworkData.setWifiAuthentication(xpp.getAttributeValue(null, "value"));
                break;
            case "net.wifi.encryption":
                MyNetworkData.setWifiEncryption(xpp.getAttributeValue(null, "value"));
                break;
            case "net.wifi.password":
                MyNetworkData.setWifiPassword(xpp.getAttributeValue(null, "value"));
                break;
            case "net.ethernet.ip":
                MyNetworkData.setEthernetIP(xpp.getAttributeValue(null, "value"));
                break;
            case "net.ethernet.dhcp.enabled":
                MyNetworkData.setEthernetDHCP(Boolean.parseBoolean(xpp.getAttributeValue(null, "value")));
                break;
            case "net.ethernet.netmask":
                MyNetworkData.setEthernetNetmask(xpp.getAttributeValue(null, "value"));
                break;
            case "net.ethernet.gateway":
                MyNetworkData.setEthernetGateway(xpp.getAttributeValue(null, "value"));
                break;
            case "net.ethernet.domain":
                MyNetworkData.setEthernetDomain(xpp.getAttributeValue(null, "value"));
                break;
            case "net.ethernet.dnsServers":
                MyNetworkData.setEthernetDNS(xpp.getAttributeValue(null, "value"));
                break;
            case "content.serverUrl":
                storeSmilIndexUrl(xpp.getAttributeValue(null, "value"));
                break;
        }
    }

    private XmlPullParser createXmlPullParser() throws XmlPullParserException
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newPullParser();
    }

    private String generateConfigXML()
    {
        return "<configuration>\n" +
                "\t<userPref>\n" +
                "\t\t<prop name=\"content.bootFromServer\" value=\"true\"/>\n" +
                "\t\t<prop name=\"content.serverUrl\" value=\""+  smil_index_url +"\"/>\n" +
                "\t</userPref>\n" +
                "</configuration>";
    }

}