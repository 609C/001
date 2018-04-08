package info.emm.LocalData;

import info.emm.messenger.FileLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ObjectOutputStream.PutField;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import android.R.anim;
import android.os.Environment;

public class FileUtil {
    public static String FILENAME=null;

    /**
     * 删除某个目录
     *
     * @param srcDir 目录地址
     * @throws IOException
     */
    public static void deleteDir(String srcDir) throws IOException{
        File file = new File(srcDir);
        if (!file.exists())
            return;

        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                deleteDir(srcDir + "/" + files[i].getName());
            deleteFile(srcDir + "/" + files[i].getName());
        }
        deleteFile(srcDir);
    }

    /**
     * 删除某个文件目录
     *
     * @param file 需要删除的文件
     */
    public static void deleteFile(File file){
        if(file != null && file.exists())
            file.delete();
    }

    /**
     * @param path 文件存储的路径
     */
    public static void deleteFile(String path){
        File file = new File(path);
        if(file != null && file.exists()){
            file.delete();
        }
    }

    /**
     * 创建某个文件目录
     *
     * @param path 目录地址
     * @return File file 返回实例化的file
     */
    public static File makeDir(String path){
        File file = new File(path);
        if(!file.exists())
            file.mkdirs();
        return file;
    }

    /**
     * 获取当前目录信息
     *
     * @param path 根目录地址
     * @return
     */
    public static List<String> getDirFile(String path){
        File file = makeDir(path);
        File[] files = file.listFiles();
        List<String> list = new ArrayList<String>();

        if(files != null && files.length != 0){
            for(int i=0;i<files.length;i++){
                list.add(files[i].getName());
            }
        }
        return list;
    }

    /**
     * 写数据到本地SD卡文件
     *
     * @param path 文件的目录
     * @param name 存储文件名
     * @param data 数据源
     * @return
     */
    public static void dateWriteToFile(String path,String name,List<String> dataList){
        String dir = Environment.getExternalStorageDirectory() + "/" + path;
        File file = new File(dir);
        if(!file.exists()){
            file.mkdirs();
        }
        File tofile = new File(dir,name);
        try {
            FileWriter fw=new FileWriter(tofile);
            BufferedWriter buffw=new BufferedWriter(fw);
            PrintWriter pw=new PrintWriter(buffw);

            for(int i=0;i<dataList.size();i++){
                pw.println(dataList.get(i) + "\n");
            }
            pw.close();
            buffw.close();
            fw.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 写数据到本地SD卡文件
     *
     * @param path
     * @param name
     * @param date
     */
    public static void dataWriteToFile(String path,String name,String data){
        if (!Environment.getExternalStorageDirectory().exists()) {
            return;
        }

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        File tofile = new File(path,name);
        try {
            FileWriter fw=new FileWriter(tofile);
            BufferedWriter buffw=new BufferedWriter(fw);
            PrintWriter pw=new PrintWriter(buffw);

            pw.println(data);
            pw.close();
            buffw.close();
            fw.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 把byte数据流文件 写入到文件中
     * 注：当没有SD卡的时候我们不写入文件直接返回了
     * @param path 文件的路径
     * @param name 文件的名字
     * @param bytes 写入文件的资源数据
     * @throws IOException
     * @return int flag [0:不存在SD卡 1:文件存在要重新调用 -1:成功]
     */
    public static int BytesWriteToFile(String path , String name , byte[] bytes) throws IOException{
        int flag = 0;
        if (!Environment.getExternalStorageDirectory().exists()) {
            return flag;
        }

        File file = new File(path , name);
        if (!file.exists())
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            //FILENAME = name;
            flag= 0;
        } else {
            flag= -1;
        }
        return flag;
    }

    /**
     * 生成写入文件的文件名
     * @return String fileName 文件名
     */
    public static String GenerateFileName(){
        long nowTime = System.currentTimeMillis();
        String fileName = nowTime+".amr";
        return fileName;
    }

    /**
     * 生写入文件的路径
     * @return String filePath 写入文件路径
     */
    public static String GenerateFilePath(){
        String filePath;
        filePath = Environment.getExternalStorageDirectory()+"/";
        return filePath;
    }

    /**
     * 生成amr音频文件的路径
     * @return
     */
    public static String getAudioPath(){
        String audio_path = Environment.getExternalStorageDirectory()+"/" + "AudioFile/";
        File file_path = new File(audio_path);
        if (!file_path.exists()) {
            file_path.mkdir();
        }
        return audio_path;
    }

    /**
     * 判断文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean fileIsExist(String path){
        File file = new File(path);
        if(file.exists()){
            return true;
        }
        return false;
    }

    /**
     * 读取文件中的内容
     */
    public static String readSDCardTxt(String path){
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(path);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 返回文件中的文件流
     * @param path 文件路径
     * @param name 文件名字
     * @return byte[] bytes 字节流
     * @throws IOException 读写文件异常
     */
    public static byte[] readFileBytes(String path , String name) throws IOException{
        File file = new File(path, name);
        FileInputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        byte[] bytes = new byte[length];
        inputStream.read(bytes);
        inputStream.close();

        return bytes;
    }
    /**
     * 根据文件名读取byte[]
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] readFileBytesPath(String path)throws IOException{
        File file = new File(path);
        FileInputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        byte[] bytes = new byte[length];
        inputStream.read(bytes);
        inputStream.close();
        return bytes;
    }

    /**
     * 返回文件的自己长度
     * @param file_path 文件路径
     * @return
     * @throws IOException
     */
    public static int readFileByteSize(String file_path) throws IOException
    {
        File file = new File(file_path);
        FileInputStream file_input_stream = new FileInputStream(file);
        int length = file_input_stream.available();
        byte[] bytes = new byte[length];
        file_input_stream.read(bytes);
        file_input_stream.close();
        return bytes.length;
    }

    /**
     * 生成一个文件
     * @param dir 生成文件的路径
     * @param filename 生成文件的名字
     * @return File file;
     */
    public static File GenerateFile(String dir , String filename){
        if (!fileIsExist(dir)) {
            makeDir(dir);
        }
        File file = new File(dir , filename);
        return file;
    }
}
