//1) Select a Outgoing ComPort of Bluetooth device and click Connect.
//
//2) If device is connected, then you can also transfer a file
//
//3) For file disconnection.

using System;

using System.Collections.Generic;

using System.ComponentModel;

using System.Data;

using System.Drawing;

using System.Drawing.Drawing2D;

using System.Drawing.Imaging;

using System.Linq;

using System.Text;

using System.Windows.Forms;

using System.IO.Ports;

using System.IO;//for Streamwriter

using System.Diagnostics;//for Debug.

using System.Runtime.InteropServices;

using System.Threading;//for Thread handling

using System.Windows.Forms;

using InTheHand.Net;

using InTheHand.Net.Sockets;

using InTheHand.Windows.Forms;

using System.Net.Sockets;

using InTheHand.Net.Bluetooth;

using System.Net;

BluetoothAddress G_address_BT;

BluetoothClient G_Client_BT;// = new BluetoothClient();

NetworkStream G_peer;

StreamWriter G_StreamWriter;// = new StreamWriter(G_peer);

namespace BluetoothDemoApp

{

public partial class frmBluetoothDemo1 : Form

{

BluetoothAddress G_address_BT;

BluetoothClient G_Client_BT;

NetworkStream G_peer;

StreamWriter G_StreamWriter;

public frmBluetoothDemo1()

{

InitializeComponent();

}

private byte[] ReadFileAsBytes(string path)

{

FileStream fs = new FileStream(path, FileMode.Open,

FileAccess.Read);

BinaryReader br = new BinaryReader(fs);

byte[] baResult = null;

try

{

baResult = new byte[fs.Length];

br.Read(baResult, 0, baResult.Length);

}

finally

{

br.Close();

fs.Close();

}

return baResult;

}

#region BT_Log

private void ClearScreen()

{

EventHandler action = delegate

{

AssertOnUiThread();

this.txtDataTx.Text = string.Empty;

};

ThreadSafeRun(action);

}

enum MessageSource

{

Local,

Remote,

Info,

Error,

}

void AddMessage(MessageSource source, string message)

{

EventHandler action = delegate

{

string prefix;

switch (source)

{

case MessageSource.Local:

prefix = "Me: ";

break;

case MessageSource.Remote:

prefix = "You: ";

break;

case MessageSource.Info:

prefix = "Info: ";

break;

case MessageSource.Error:

prefix = "Error: ";

break;

default:

prefix = "???:";

break;

}

AssertOnUiThread();

this.txtDataTx.Text =

prefix + message + "\r\n"

+ this.txtDataTx.Text;

};

ThreadSafeRun(action);

}

private void ThreadSafeRun(EventHandler action)

{

Control c = this.txtDataTx;

if (c.InvokeRequired)

{

c.BeginInvoke(action);

}

else

{

action(null, null);

}

}

#endregion

private void AssertOnUiThread()

{

Debug.Assert(!this.txtDataTx.InvokeRequired, "UI access from non

UI thread!");

}

BluetoothAddress BluetoothSelect()

{

var dlg = new SelectBluetoothDeviceDialog();

var rslt = dlg.ShowDialog();

if (rslt != DialogResult.OK)

{

AddMessage(MessageSource.Info, "Cancelled select device.");

return null;

}

var addr = dlg.SelectedDevice.DeviceAddress;

lstSelectedDevice.Items.Clear();

lstSelectedDevice.Items.Add(dlg.SelectedDevice.DeviceName + " :"

+ dlg.SelectedDevice.DeviceAddress);

return addr;

}

private void butConnect_Click(object sender, EventArgs e)

{

G_address_BT = BluetoothSelect();//var addr = BluetoothSelect();

if (G_address_BT == null)

{

return;

}



// BluetoothConnect(addr);

// BluetoothAddress addr = ... e.g. selected.DeviceAddress;

G_Client_BT = new BluetoothClient();//var cli = new

BluetoothClient();

G_Client_BT.Connect(G_address_BT, BluetoothService.SerialPort);

//using (var peer = G_Client_BT.GetStream())

//using (var wtr = new StreamWriter(peer))

//{

// wtr.WriteLine("Hello world");

//}

G_peer = G_Client_BT.GetStream();

if (G_Client_BT.Connected)

{

G_StreamWriter = new StreamWriter(G_peer);//var wtr = new

StreamWriter(G_peer);

//G_StreamWriter.WriteLine("Hello world via Bluetooth Communication...");

G_StreamWriter.AutoFlush = true;

butConnect.Enabled = false;

butDisconnect.Enabled = true;

butSend.Enabled = true;

butTransferFile.Enabled = true;

}

}

private void butDisconnect_Click(object sender, EventArgs e)

{

G_Client_BT.Close();

G_peer.Close();

G_StreamWriter.Close();

butConnect.Enabled = true;

butDisconnect.Enabled = false;

butSend.Enabled = false;

butTransferFile.Enabled = false;

}

private void butSendData_Click(object sender, EventArgs e)

{

if (G_Client_BT.Connected == false)

{

G_Client_BT = new BluetoothClient();

G_Client_BT.Connect(G_address_BT,

BluetoothService.SerialPort);

G_peer = G_Client_BT.GetStream();

G_StreamWriter = new StreamWriter(G_peer);//var wtr = new

StreamWriter(G_peer);

G_StreamWriter.AutoFlush = true;

//G_StreamWriter.WriteLine("Hello world via Bluetooth Communication...");

}

if (G_Client_BT.Connected)

{

G_StreamWriter.WriteLine(txtDataTx.Text);

//G_StreamWriter.AutoFlush = true;

//G_StreamWriter.Flush(); // if autoflush is false then write focefully

}

}

private void butTransferFile_Click(object sender, EventArgs e)

{

OpenFileDialog ofdTextPrint_Browse = new OpenFileDialog();

ofdTextPrint_Browse.Filter = "Text Files (.txt)|*.txt|All Files (.*)|*.*";

DialogResult dr = ofdTextPrint_Browse.ShowDialog();

if (dr == DialogResult.OK)

{

txtDataTx.Text = "";

lblFile.Text = ofdTextPrint_Browse.FileName;

byte[] fileBytes =

ReadFileAsBytes(ofdTextPrint_Browse.FileName);

int idx, iLen, iCount;

idx = 0;

iLen = fileBytes.Length;

Encoding enc1252 = Encoding.GetEncoding(1252);

while (idx < iLen)

{//serialPort1.Write(txtSend.Text);

iCount = ((iLen - idx) > 512 ? 512 : (iLen - idx));

//serialPort1.Write(fileBytes, idx, iCount); // from serialport

if (G_Client_BT.Connected)//from socket

{

//G_StreamWriter.Write(txtDataTx.Text);

//G_StreamWriter.Write(fileBytes, idx, iCount);

char[] chars = enc1252.GetChars(fileBytes, idx, iCount);

G_StreamWriter.Write(chars, 0, iCount);



//G_StreamWriter.AutoFlush = true;

//G_StreamWriter.Flush(); // if autoflush is false then write

focefully

}

txtDataTx.Text = txtDataTx.Text +

Encoding.ASCII.GetString(fileBytes, idx, iCount);

idx = idx + 512;

}

}

else if (dr == DialogResult.Cancel)

{

}

}

private void butClear_Click(object sender, EventArgs e)

{

this.txtDataTx.Text = "";

}

private void ReadMessagesToEnd(Stream peer)

{

var rdr = new StreamReader(peer);

while (true)

{

string line;

try

{

line = rdr.ReadLine();//.BaseStream.Length;

if (line.Length > 0)

{

DisplayOnOutputScreen(LogMsgType.Incoming, line);

}

}

catch (IOException ioex)

{

{

DisplayOnOutputScreen(LogMsgType.Error , "Connection

was closed hard (read). " + ioex.Message);

}

break;

}



}

}

namespace PrinterTestPcApp

{

#region Public Enumerations

//public enum DataMode { Text, Hex }

public enum LogMsgType { Incoming, Outgoing, Normal, Warning, Error

};

#endregion

public partial class frmThermalPrinter : Form

{

// Various colors for logging info

private Color[] LogMsgTypeColor = { Color.Blue, Color.Green,

Color.Black, Color.Orange, Color.Red };

private string selectedCOM = "";

private int selectedBaud = 0;

private string filedataTextPrint = "";

private byte[] filedataBitmapImagePrint;

bool bHexMode = false;

private static bool flagDirection = true;

private static bool flagQRCodePrint = false;

private System.Windows.Forms.DomainUpDown UPDOWN_DOMAIN;

[DllImport("user32.dll")]

static extern IntPtr LoadImage(IntPtr hinst, string lpszName,

uint uType, int cxDesired, int cyDesired, uint fuLoad);

[DllImport("gdi32.dll")]

static extern bool DeleteObject(IntPtr hObject);

private const int LR_LOADFROMFILE = 0x0010;

private const int LR_MONOCHROME = 0x0001;

private string FileName_BitmapPrint = "";

public frmThermalPrinter()

{

InitializeComponent();

}

private void Form1_Load(object sender, EventArgs e)

{

string[] ports = SerialPort.GetPortNames();

if (ports.Length > 0)

{

for (int i = 0; i < ports.Length; i++)

{

cmbCOM.Items.Add(ports[i]);

}

}

btnOpenPort.Enabled = true;

btnClosePort.Enabled = false;

if (cmbCOM.Items.Count > 0)

{

cmbCOM.SelectedIndex = 0;

}

if (cmbBaud.Items.Count > 0)

{

cmbBaud.SelectedIndex = 3;

}

rdbTextPrint_Default.Enabled = false;

rdbTextPrint_Browse.Enabled = false;

rdbBitmapPrint_Default.Enabled = false;

rdbBitmapPrint_Browse.Enabled = false;

dtpWaitingTime.Format = DateTimePickerFormat.Custom;

dtpWaitingTime.CustomFormat = "HH:mm";

//set default value

dtpWaitingTime.Value = Convert.ToDateTime("0:05");

}

private void DelayMs(double ulDelayMs)

{

double ulCurTimeMs;//DateTime

double DurationMs;//TimeSpan

//double dblTotMs;

//string str;

ulCurTimeMs = Environment.TickCount;

//str = "\r\n START:" + DateTime.Now.Minute + ":" +

DateTime.Now.Second + "." + (ulCurTimeMs % 1000);

//LogData(rtflModemActivity, str);

while (true)

{

DurationMs = (Environment.TickCount - ulCurTimeMs);

if (DurationMs < 0)

DurationMs = DurationMs * -1;

if (DurationMs < ulDelayMs)

Application.DoEvents();

else

break;

}

}

/// <summary> Convert a string of hex digits (ex: E4 CA B2) to a byte

array. </summary>

/// <param name="s"> The string containing the hex digits (with or

without spaces). </param>

/// <returns> Returns an array of bytes. </returns>

private byte[] HexStringToByteArray(string s)

{

s = s.Replace(" ", "");

byte[] buffer = new byte[s.Length / 2];

for (int i = 0; i < s.Length; i += 2)

buffer[i / 2] = (byte)Convert.ToByte(s.Substring(i, 2), 16);

return buffer;

}

/// <summary> Converts an array of bytes into a formatted string of hex

/// digits (ex: 0x41 0x42 0x43 as "414243")</summary>

/// <param name="data"> The array of bytes to be translated into a

/// string of hex digits.</param>

/// <returns> Returns a well formatted string of hex digits without

/// spacing.</returns>

public string ByteArr2HexStr(byte[] data, int SpacePadding = 0)

{

int i;

StringBuilder sb = new StringBuilder(data.Length * 2);

for (i = 0; (i < data.Length); i++)

{

//sb.Append(Convert.ToString(b, 16).PadLeft(2, '0').PadRight(3, '

'));// with Space

if (SpacePadding == 0)

sb.Append(Convert.ToString(data[i], 16).PadLeft(2, '0'));//

without Space

else

sb.Append(Convert.ToString(data[i], 16).PadLeft(2,

'0').PadRight(3, ' '));// with Space

}

return sb.ToString().ToUpper();

}

/// <summary> Converts an array of bytes into a formatted string of hex

digits (ex: E4 CA B2)</summary>

/// <param name="data"> The array of bytes to be translated into a

string of hex digits. </param>

/// <returns> Returns a well formatted string of hex digits with spacing.

</returns>

private string ByteArrayToHexString(byte[] data)

{

StringBuilder sb = new StringBuilder(data.Length * 3);

foreach (byte b in data)

sb.Append(Convert.ToString(b, 16).PadLeft(2, '0').PadRight(3, '

'));

return sb.ToString().ToUpper();

}

/// <summary> Converts a string into array of bytes

/// (ex: "ABC" as 0x41 0x42 0x43)</summary>

/// <param name="bytBuff"> The string to be translated into a

/// array of bytes.</param>

/// <returns> Returns an array of bytes.</returns>

public byte[] Str2BytArr(string strData)

{//Converting a String to a byte-array



Encoding enc1252 = Encoding.GetEncoding(1252);

return enc1252.GetBytes(strData);

}

private void cmbCOM_SelectedIndexChanged(object sender, EventArgs e)

{

if (cmbCOM.Items.Count > 0)

{

selectedCOM = cmbCOM.Text;

}

}

private void cmbBaud_SelectedIndexChanged(object sender,

EventArgs e)

{

if (cmbBaud.Items.Count > 0)

{

selectedBaud = Convert.ToInt32(cmbBaud.Text);

}

}

private void btnOpenPort_Click(object sender, EventArgs e)

{

if (!serialPort1.IsOpen)

{

serialPort1.PortName = selectedCOM;

serialPort1.BaudRate = selectedBaud;

serialPort1.Open();

btnOpenPort.Enabled = false;

btnClosePort.Enabled = true;

tabControl1.Enabled = true;

}

else

{

MessageBox.Show("Port Already Open");

}

}

private void btnClosePort_Click(object sender, EventArgs e)

{

if (serialPort1.IsOpen)

{

serialPort1.Close();

btnClosePort.Enabled = false;

btnOpenPort.Enabled = true;

tabControl1.Enabled = false ;

}

}

private void Form1_FormClosing(object sender,

FormClosingEventArgs e)

{

if (serialPort1.IsOpen)

{

serialPort1.Close();

}

}

private void rdbTextPrint_Browse_CheckedChanged(object sender,

EventArgs e)

{

if (rdbTextPrint_Browse.Checked)

{

OpenFileDialog ofdTextPrint_Browse = new OpenFileDialog();

ofdTextPrint_Browse.Filter = "Text Files (.txt)|*.txt";

DialogResult dr=ofdTextPrint_Browse.ShowDialog();

if (dr == DialogResult.OK)

{

txtTextPrint_Browse.Text = ofdTextPrint_Browse.FileName;

byte[] fileBytes =

ReadFileAsBytes(ofdTextPrint_Browse.FileName);

filedataTextPrint = Encoding.ASCII.GetString(fileBytes) + "\n";

}

else if (dr == DialogResult.Cancel)

{

rdbTextPrint_Browse.Checked = false;

rdbTextPrint_Default.Checked = true;

}

}

}

private byte[] ReadFileAsBytes(string path)

{

FileStream fs = new FileStream(path, FileMode.Open,

FileAccess.Read);

BinaryReader br = new BinaryReader(fs);

byte[] baResult = null;

try

{

baResult = new byte[fs.Length];

br.Read(baResult, 0, baResult.Length);

}

finally

{

br.Close();

fs.Close();

}

return baResult;

}

private void rdbBitmapPrint_Default_CheckedChanged(object sender,

EventArgs e)

{

if (rdbBitmapPrint_Default.Checked)

{

//byte[] defaultAemLogo = new byte[] { 0x1B, 0x2A, 0x65, 0x6C,

0x0F, 0x13, 0x00, 0x00, 0x07, 0x3F, 0xF0, 0xFF, 0xFF, 0xFF, 0xFF, 0xF0,

0xFF, 0xCF, 0xFC, 0xFF, 0xC0, 0x00, 0x00, 0x0F, 0x30, 0x30, 0xC0, 0x00,

0x00, 0x00, 0x30, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00, 0x00, 0x1B, 0x30,

0x30, 0xC0, 0x00, 0x00, 0x00, 0x30, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00,

0x00, 0x33, 0x30, 0x30, 0xC0, 0x00, 0x00, 0x00, 0x30, 0xC0, 0xCC, 0x0C,

0xC0, 0xC0, 0x00, 0x00, 0x63, 0x30, 0x30, 0xFF, 0xFF, 0xFF, 0xFF, 0xF0,

0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00, 0x00, 0xC3, 0x30, 0x30, 0x00, 0x00,

0x00, 0x00, 0x00, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00, 0x01, 0x83, 0x30,

0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00,

0x03, 0x03, 0x30, 0x30, 0xFF, 0xFF, 0xFF, 0xFF, 0xF0, 0xC0, 0xCC, 0x0C,

0xC0, 0xC0, 0x00, 0x06, 0x03, 0x30, 0x30, 0xC0, 0x00, 0x00, 0x00, 0x30,

0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00, 0x0C, 0x03, 0x30, 0x30, 0xC0, 0x00,

0x00, 0x00, 0x30, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00, 0x18, 0x03, 0x30,

0x30, 0xC0, 0x00, 0x00, 0x00, 0x30, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00,

0x30, 0x03, 0x30, 0x30, 0xFF, 0xFF, 0xFF, 0xFF, 0xF0, 0xC0, 0xCC, 0x0C,

0xC0, 0xC0, 0x00, 0x60, 0x03, 0x30, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00,

0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x00, 0xC0, 0x03, 0x30, 0x30, 0x00, 0x00,

0x00, 0x00, 0x00, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x01, 0x80, 0x03, 0x30,

0x30, 0xFF, 0xFF, 0xFF, 0xFF, 0xF0, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0,

0x03, 0x00, 0x03, 0x30, 0x30, 0xC0, 0x00, 0x00, 0x00, 0x30, 0xC0, 0xCC,

0x0C, 0xC0, 0xC0, 0x06, 0x00, 0x03, 0x30, 0x30, 0xC0, 0x00, 0x00, 0x00,

0x30, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x0C, 0x00, 0x03, 0x30, 0x30, 0xC0,

0x00, 0x00, 0x00, 0x30, 0xC0, 0xCC, 0x0C, 0xC0, 0xC0, 0x1F, 0xFF, 0xFF,

0x3F, 0xF0, 0xFF, 0xFF, 0xFF, 0xFF, 0xF0, 0xFF, 0xCF, 0xFC, 0xFF, 0xC0

};

//filedataBitmapImagePrint = defaultAemLogo;

txtBitmapPrint_Browse.Text = "";

FileName_BitmapPrint = Application.StartupPath +

"\\RES_Files\\Default_Bitmap.bmp";

//printBitmapImage(FileName_BitmapPrint);

}

else

FileName_BitmapPrint = "";

printBitmapImage(FileName_BitmapPrint);

}

private void rdbBitmapPrint_Browse_CheckedChanged(object sender,

EventArgs e)

{

if (rdbBitmapPrint_Browse.Checked)

{

FileName_BitmapPrint = "";// Application.StartupPath +

"\\RES_Files\\AEM_QRCode.bmp";

ofdSelect.Filter =

"BMP|*.bmp|GIF|*.gif|JPG|*.jpg;*.jpeg|PNG|*.png|TIFF|*.tif;*.tiff|" + "All

Images|*.bmp;*.jpg;*.jpeg;*.png;*.tif;*.tiff"; //"bmp files (*.bmp)|*.bmp";

if (SelectFile())

{

txtBitmapPrint_Browse.Text = ofdSelect.FileName;

FileName_BitmapPrint = ofdSelect.FileName;

//printBitmapImage(FileName_BitmapPrint);

}

else

{//set default name

// Application.StartupPath + "\\RES_Files\\AEM_QRCode.bmp";

FileName_BitmapPrint = "";

rdbBitmapPrint_Browse.Checked = false;

rdbBitmapPrint_Default.Checked = true;

//printBitmapImage(FileName_BitmapPrint);

}

}

else

FileName_BitmapPrint = "";//default name

printBitmapImage(FileName_BitmapPrint);

}

}

private System.Windows.Forms.RadioButton rdbBitmapPrint_Default;

public void printBitmapImage(string sFileName)

{

if (sFileName.Length == 0)

{

filedataBitmapImagePrint = new byte[0];

return;

}

if (rdbBitmapPrint_Default.Enabled == true)

{

WriteOnPrinter("Printing Default Bitmap ....\n\n");

}

string tempFilePath =

ResizeAndSaveBitmapAsMonochrome(sFileName);

byte[] bitmapBytes = ReadFileAsBytes(tempFilePath);

MakeBmp1bpp mkBMP = new MakeBmp1bpp();

byte[] bytRawBuf1bpp =

mkBMP.Convert_BMP_To_1bppMonochrome(bitmapBytes);

filedataBitmapImagePrint = SendImageDataToPrinter(bytRawBuf1bpp);

File.Delete(tempFilePath);

}

static string ResizeAndSaveBitmapAsMonochrome(string fileName)

{

//a temp file is required for storing the source file as BMP,

//since the ageing GDI LoadImage fails to recognize other formats

string tempFile = System.IO.Path.GetTempFileName();

tempFile = System.IO.Path.ChangeExtension(tempFile, "bmp");

//the destination file -- any checks are omitted

//for the sake of briefness

string dstFile = System.IO.Path.GetDirectoryName(fileName) + "\\"

+ System.IO.Path.GetFileNameWithoutExtension(fileName) +

"_mono.bmp";

System.Drawing.Bitmap b;

try

{

//loading bitmap from the source image file

//and then saving it in the temp .BMP file

b = new Bitmap(fileName);

if (b.Width > 384 && b.Height > 255 )//|| b.Width > 384 || b.Height

> 255)

{

Bitmap resizedBitmap = new Bitmap(ResizeImage(b, new

Size(384, 255)));

b = resizedBitmap;

}

else if (b.Width <= 384 && b.Height > 255)

{

Bitmap resizedBitmap = new Bitmap(ResizeImage(b, new

Size(b.Width, 255)));

b = resizedBitmap;

}

else if (b.Width > 384 && b.Height <= 255)

{

Bitmap resizedBitmap = new Bitmap(ResizeImage(b, new

Size(384, b.Height)));

b = resizedBitmap;

}

b.Save(tempFile, System.Drawing.Imaging.ImageFormat.Bmp);

}

catch (Exception e)

{

Console.WriteLine(e.Message);

return null;

}

try

{

IntPtr hBitmap = LoadImage(IntPtr.Zero, tempFile, 0, 0, 0,

(LR_LOADFROMFILE | LR_MONOCHROME));



//creating Image object from HBITMAP handle

//and saving it to destination file

Image img = Image.FromHbitmap(hBitmap);

img.Save(dstFile, System.Drawing.Imaging.ImageFormat.Bmp);

//cleaning up

DeleteObject(hBitmap);

System.IO.File.Delete(tempFile);

}

catch (Exception e)

{

Console.WriteLine(e.Message);

return null;

}

Console.WriteLine("Successfully converted to:");

Console.WriteLine(dstFile);

return dstFile;

}

byte[] SendImageDataToPrinter(byte[] bytBmpBuf1bpp)

{

byte[] bitmapBytes = bytBmpBuf1bpp;

CopyBufToStructBMP(bitmapBytes, ref objBmpInfo); // function is in

MakeBmp1bpp class

uint actualWidth = objBmpInfo.nHorizontalLen;

uint offset = objBmpInfo.Byte_Offset;

uint actualHeight = objBmpInfo.nVeritcalLen;

uint totalSize = objBmpInfo.Totalsize;

uint reWidth = (objBmpInfo.Totalsize - objBmpInfo.Byte_Offset) /

actualHeight;

uint pad = reWidth * 8 - actualWidth;

byte[] dataWithoutHeader = new byte[bitmapBytes.Length - 62];

for (uint i = offset; i < bitmapBytes.Length; i++)

{

dataWithoutHeader[i - 62] = bitmapBytes[i];

}

byte[] imagePacket = new byte[dataWithoutHeader.Length + 6];

imagePacket[0] = 0x1B;//[ESC]

imagePacket[1] = 0x2A;// *

imagePacket[2] = 0x64;// m single width, single height

imagePacket[3] = 0x6C;// align Left align

imagePacket[4] = Convert.ToByte(reWidth);

imagePacket[5] = Convert.ToByte(actualHeight);

uint a = 0;

int b = 0;

byte[,] temp = new byte[actualHeight, reWidth];

for (int i = 0; i < actualHeight; i++)

{

bool flag = true;

for (int j = 0; j < reWidth; j++)

{

if (j < actualWidth / 8)

{

temp[i, j] = (byte)(0xFF ^

dataWithoutHeader[dataWithoutHeader.Length - (reWidth) * (i + 1) + j]);

}

else

{

int c = (int)actualWidth % 8;

if (c != 0 && flag)

{

byte byt = (byte)(0xFF << (8 - c));

temp[i, j] = (byte)(byt ^

dataWithoutHeader[dataWithoutHeader.Length - (reWidth) * (i + 1) + j]);

byt = temp[i, j];

flag = false;

}

else

{

temp[i, j] =

dataWithoutHeader[dataWithoutHeader.Length - (reWidth) * (i + 1) + j];

}

}

imagePacket[a + 6] = temp[i, j];

a++;

}

}

private byte[] ReadFileAsBytes(string path)

{

FileStream fs = new FileStream(path, FileMode.Open,

FileAccess.Read);

BinaryReader br = new BinaryReader(fs);

byte[] baResult = null;

try

{

baResult = new byte[fs.Length];

br.Read(baResult, 0, baResult.Length);

}

finally

{

br.Close();

fs.Close();

}

return baResult;

}