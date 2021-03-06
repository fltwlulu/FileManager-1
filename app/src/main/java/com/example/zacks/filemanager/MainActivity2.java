package com.example.zacks.filemanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




/**
 * Created by zacks on 2018/1/23.
 *
 */

public class MainActivity2
        extends AppCompatActivity
        implements FileAdapter1.OnItemListener,NavigationView.OnNavigationItemSelectedListener{

    private String keyWords;
    private Toolbar mToolbar;
    private RecyclerView mRv1;

    private FileAdapter1 mAdapter1;
    private List<String> mFileNames ;
    private List<String> mFilePaths ;

    private String mRootPath = Environment.getRootDirectory().getPath();
    private String mSDCard = Environment.getExternalStorageDirectory().toString();
    private String mOldFilePath = "";
    private String mNewFilePath = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        design();
        initRv1();
    }

    private void initRv1() {
        mRv1=findViewById(R.id.rv_1);
        mAdapter1 = new FileAdapter1(mFileNames, mFilePaths, this);
        mRv1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        initFileListInfo(mRootPath);
    }

    private void design() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        TabLayout tab=findViewById(R.id.tab_layout);
        tab.addTab(tab.newTab().setText("根目录"));
        tab.addTab(tab.newTab().setText("储存"));
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==0){
                    initFileListInfo(mRootPath);
                }else {
                    initFileListInfo(mSDCard);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               createFolder();
            }
        });

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawer, mToolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * 粘贴
     */
    private void palseFile() {
        mNewFilePath = mCurrentFilePath + java.io.File.separator + mCopyFileName;//得到新路径
        if (!mOldFilePath.equals(mNewFilePath) && isCopy) {//在不同路径下复制才起效
            if (!new File(mNewFilePath).exists()) {
                copyFile(mOldFilePath, mNewFilePath);
                Toast.makeText(getApplicationContext(), "执行了粘贴", Toast.LENGTH_SHORT).show();
                initFileListInfo(mCurrentFilePath);
            } else {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("提示!")
                        .setMessage("该文件名已存在，是否要覆盖?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                copyFile(mOldFilePath, mNewFilePath);
                                initFileListInfo(mCurrentFilePath);
                            }
                        })
                        .setNegativeButton("取消", null).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "未复制文件！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 复制
     * */
    private void copyFile(String oldFile, String newFile) {
        int i;
        FileInputStream fis;
        FileOutputStream fos;
        try {
            fis = new FileInputStream(oldFile);
            fos = new FileOutputStream(newFile);
            do {
                //逐个byte读取文件，并写入另一个文件中
                if ((i = fis.read()) != -1) {
                    fos.write(i);
                }
            } while (i != -1);
            //关闭输入文件流
            fis.close();
            //关闭输出文件流
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String mNewFolderName = "";
    private File mCreateFile;
    private RadioGroup mCreateRadioGroup;
    private static int mChecked;
    /**
     * 创建文件夹的方法:当用户点击软件下面的创建菜单的时候，
     * 是在当前目录下创建的一个文件夹
     * 静态变量mCurrentFilePath存储的就是当前路径
     * java.io.File.separator是JAVA给我们提供的一个File类中的静态成员，
     * 它会根据系统的不同来创建分隔符
     * mNewFolderName正是我们要创建的新文件的名称，从EditText组件上得到的
     */
    private void createFolder() {
        //用于标识当前选中的是文件或者文件夹
        mChecked = 2;
        LayoutInflater mLI = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //初始化对话框布局
        final LinearLayout mLL = (LinearLayout) mLI.inflate(R.layout.create_dialog, null);
        mCreateRadioGroup =  mLL.findViewById(R.id.radiogroup_create);
        final RadioButton mCreateFileButton =  mLL.findViewById(R.id.create_file2);
        final RadioButton mCreateFolderButton =  mLL.findViewById(R.id.create_folder);
        //设置默认为创建文件夹
        mCreateFolderButton.setChecked(true);
        //为按钮设置监听器
        mCreateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //当选择改变时触发
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                if (arg1 == mCreateFileButton.getId()) {
                    mChecked = 1;
                } else if (arg1 == mCreateFolderButton.getId()) {
                    mChecked = 2;
                }
            }
        });
        //显示对话框
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this)
                .setTitle("新建")
                .setView(mLL)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //或者用户输入的名称
                        mNewFolderName =
                                ((EditText) mLL.findViewById(R.id.new_filename))
                                        .getText().toString();
                        if (mChecked == 1) {
                            try {
                                mCreateFile = new File(mCurrentFilePath
                                        + java.io.File.separator + mNewFolderName + ".txt");
                                mCreateFile.createNewFile();
                                //刷新当前目录文件列表
                                initFileListInfo(mCurrentFilePath);
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "文件名拼接出错..!!", Toast.LENGTH_SHORT).show();
                            }
                        } else if (mChecked == 2) {
                            mCreateFile = new File(
                                    mCurrentFilePath + java.io.File.separator + mNewFolderName);
                            if (!mCreateFile.exists() && !mCreateFile.isDirectory() &&
                                    mNewFolderName.length() != 0) {
                                if (mCreateFile.mkdirs()) {
                                    //刷新当前目录文件列表
                                    initFileListInfo(mCurrentFilePath);
                                } else {
                                    Toast.makeText(getApplicationContext(), "创建失败，可能是系统权限不够，root一下？！", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "文件名为空，还是重名了呢？", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setNeutralButton("取消", null);
        mBuilder.show();
    }

    Intent serviceIntent;
    RadioGroup mRadioGroup;
    static int mRadioChecked;
    public static final String KEYWORD_BROADCAST = "KEYWORD_BROADCAST";
    //显示搜索对话框
    private void searchDialog() {
        //用于确定是在当前目录搜索或者是在整个目录搜索的标志
        mRadioChecked = 1;
        LayoutInflater mLI = LayoutInflater.from(this);
        final View mLL =  mLI.inflate(R.layout.search_dialog, null);
        mRadioGroup = mLL.findViewById(R.id.radiogroup_search);
        final RadioButton mCurrentPathButton =  mLL.findViewById(R.id.radio_currentpath);
        final RadioButton mWholePathButton =  mLL.findViewById(R.id.radio_wholepath);
        //设置默认选择在当前路径搜索
        mCurrentPathButton.setChecked(true);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //当选择改变时触发
            public void onCheckedChanged(RadioGroup radiogroup, int checkId) {
                //当前路径的标志为1
                if (checkId == mCurrentPathButton.getId()) {
                    mRadioChecked = 1;
                    //整个目录的标志为2
                } else if (checkId == mWholePathButton.getId()) {
                    mRadioChecked = 2;
                }
            }
        });
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this)
                .setTitle("搜索").setView(mLL)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        keyWords = ((EditText) mLL.findViewById(R.id.edit_search)).getText().toString();
                        if (keyWords.length() == 0) {
                            Toast.makeText(getApplicationContext(), "关键字不能为空!", Toast.LENGTH_SHORT).show();
                            searchDialog();
                        } else {
                            if (menuPosition == 1) {
                                mToolbar.setTitle(mRootPath);
                            } else {
                                mToolbar.setTitle(mSDCard);
                            }
                            //获取用户输入的关键字并发送广播-开始
                            Intent keywordIntent = new Intent();
                            keywordIntent.setAction(KEYWORD_BROADCAST);
                            //传递搜索的范围区间:1.当前路径下搜索 2.SD卡下搜索
                            if (mRadioChecked == 1) {
                                keywordIntent.putExtra("searchpath", mCurrentFilePath);
                            } else {
                                keywordIntent.putExtra("searchpath", mSDCard);
                            }
                            //传递关键字
                            keywordIntent.putExtra("keyword", keyWords);
                            //到这里为止是携带关键字信息并发送了广播，会在Service服务当中接收该广播并提取关键字进行搜索
                            getApplicationContext().sendBroadcast(keywordIntent);
                            //获取用户输入的关键字并发送广播-结束
                            serviceIntent = new Intent("com.android.service.FILE_SEARCH_START");
                            startService(serviceIntent);//开启服务，启动搜索
                            isComeBackFromNotification = false;
                        }
                    }
                })
                .setNegativeButton("取消", null);
        mBuilder.create().show();
    }



    //1代表手机，2代表SD卡
    private static int menuPosition = 1;
    //用静态变量存储 当前目录路径信息
    public static String mCurrentFilePath = "";
    //是否需要添加BackUp键
    private boolean isAddBackUp = false;
    /**
     * 根据给定的一个文件夹路径字符串遍历出这个文件夹中包含的文件名称并配置到列表中
     * 这个函数主要做了以下事情
     * 1 判断当前目录是否需要添加 “返回根目录”和“返回上一级”
     * 2 遍历出该文件夹路径下的所有文件/文件夹
     * 3 显示当前的路径
     * 4 将所有文件信息添加到集合中
     * 5 更新数据、监听器
     */
    private void initFileListInfo(String filePath) {
        isAddBackUp = false;
        mCurrentFilePath = filePath;
        mToolbar.setTitle(filePath);
        mFileNames = new ArrayList<>();
        mFilePaths = new ArrayList<>();
        File mFile = new File(filePath);
        File[] mFiles = mFile.listFiles();
        if (menuPosition == 1 && !mCurrentFilePath.equals(mRootPath)) {
            initAddBackUp(filePath, mRootPath);
        } else if (menuPosition == 2 && !mCurrentFilePath.equals(mSDCard)) {
            initAddBackUp(filePath, mSDCard);
        }
        for (File mCurrentFile : mFiles) {
            mFileNames.add(mCurrentFile.getName());
            mFilePaths.add(mCurrentFile.getPath());
        }
        mAdapter1.upData(mFileNames, mFilePaths);
        mAdapter1.setOnItemClickListener(this);
        mRv1.setAdapter(mAdapter1);
    }

    /**
     * 根据点击“手机”还是“SD卡”来加“返回根目录”和“返回上一级”
     */
    private void initAddBackUp(String filePath, String phone_sdcard) {
        if (!filePath.equals(phone_sdcard)) {
            //列表项的第一项设置为返回根目录
            mFileNames.add("BacktoRoot");
            mFilePaths.add(phone_sdcard);
    		//列表项的第二项设置为返回上一级
            mFileNames.add("BacktoUp");
            //回到当前目录的父目录即回到上级
            mFilePaths.add(new File(filePath).getParent());
            //将添加返回按键标识位置为true
            isAddBackUp = true;
        }
    }

    String txtData = "";
    boolean isTxtDataOk = false;
    ProgressDialog mProgressDialog;
    boolean isCancleProgressDialog = false;
    @Override
    public void onItemClick(View view, int position) {
        final File mFile = new File(mFilePaths.get(position));
        //如果该文件是可读的，我们进去查看文件
        if (mFile.canRead()) {
            if (mFile.isDirectory()) {
                //如果是文件夹，则直接进入该文件夹，查看文件目录
                initFileListInfo(mFilePaths.get(position));
            } else {
                //如果是文件，则用相应的打开方式打开
                String fileName = mFile.getName();
                String fileEnds = fileName.substring(
                        fileName.lastIndexOf(".") + 1, fileName.length())
                        .toLowerCase();
                if (fileEnds.equals("txt")) {
                    //显示进度条，表示正在读取
                    initProgressDialog(ProgressDialog.STYLE_HORIZONTAL);
                    new Thread(new Runnable() {
                        public void run() {
                            //打开文本文件
                            openTxtFile(mFile.getPath());
                        }
                    }).start();
                    new Thread(new Runnable() {
                        public void run() {
                            while (true) {
                                if (isTxtDataOk) {
                                    //关闭进度条
                                    mProgressDialog.dismiss();
                                    executeIntent(txtData, mFile.getPath());
                                    break;
                                }
                                if (isCancleProgressDialog) {
                                    //关闭进度条
                                    mProgressDialog.dismiss();
                                    break;
                                }
                            }
                        }
                    }).start();
                    //如果是html文件则用自己写的工具打开
                } else if (fileEnds.equals("html") || fileEnds.equals("mht") || fileEnds.equals("htm")) {
                    Intent intent = new Intent(this, WebActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("filePath", mFile.getPath());
                    startActivity(intent);
                } else {
                    openFile(mFile);
                }
            }
        } else {
            //如果该文件不可读，我们给出提示不能访问，防止用户操作系统文件造成系统崩溃等
            Toast.makeText(this, "对不起，您的访问权限不足!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获得MIME类型的方法
     */
    private String getMIMEType(File file) {
        String type ;
        String fileName = file.getName();
        //取出文件后缀名并转成小写
        String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        if (fileEnds.equals("m4a") || fileEnds.equals("mp3") || fileEnds.equals("mid") || fileEnds.equals("xmf") || fileEnds.equals("ogg") || fileEnds.equals("wav")) {
            type = "audio/*";// 系统将列出所有可能打开音频文件的程序选择器
        } else if (fileEnds.equals("3gp") || fileEnds.equals("mp4")) {
            type = "video/*";// 系统将列出所有可能打开视频文件的程序选择器
        } else if (fileEnds.equals("jpg") || fileEnds.equals("gif") || fileEnds.equals("png") || fileEnds.equals("jpeg") || fileEnds.equals("bmp")) {
            type = "image/*";// 系统将列出所有可能打开图片文件的程序选择器
        } else {
            type = "*/*"; // 系统将列出所有可能打开该文件的程序选择器
        }
        return type;
    }

    /**
     * 调用系统的方法，来打开文件的方法
     */
    private void openFile(File file) {
        if (file.isDirectory()) {
            initFileListInfo(file.getPath());
        } else {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            //设置当前文件类型
            intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
            startActivity(intent);
        }
    }

    private void executeIntent(String data, String file) {
        Intent intent = new Intent(this, EditTxtActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //传递文件的路径，标题和内容
        intent.putExtra("path", file);
        intent.putExtra("title", new File(file).getName());
        intent.putExtra("data", data);
        //跳转到EditTxtActivity
        startActivity(intent);
    }

    /**
     * 弹出正在解析文本数据的ProgressDialog
     */
    private void initProgressDialog(int style) {
        isCancleProgressDialog = false;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("正在为你解析文本数据，请稍后...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                isCancleProgressDialog = true;
                mProgressDialog.dismiss();
            }
        });
        mProgressDialog.show();
    }

    //打开文本文件的方法之读取文件数据
    private void openTxtFile(String file) {
        isTxtDataOk = false;
        try {
            FileInputStream fis =
                    new FileInputStream(new File(file));
            StringBuilder mSb = new StringBuilder();
            int m;
            //读取文本文件内容
            while ((m = fis.read()) != -1) {
                mSb.append((char) m);
            }
            fis.close();
            //保存读取到的数据
            txtData = mSb.toString();
            //读取完毕
            isTxtDataOk = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        if (isAddBackUp) {//说明存在返回根目录和返回上一级两列，接下来要对这两列进行屏蔽
            if (position != 0 && position != 1) {
                initItemLongClickListener(new File(mFilePaths.get(position)));
            }
        }
        if (mCurrentFilePath.equals(mRootPath) || mCurrentFilePath.equals(mSDCard)) {
            initItemLongClickListener(new File(mFilePaths.get(position)));
        }
    }


    private String mCopyFileName;
    private boolean isCopy = false;

    /**
     * 长按文件或文件夹时弹出的带效果的功能菜单
     */
    private void initItemLongClickListener(final File file) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            //item的值就是从0开始的索引值(从列表的第一项开始)
            public void onClick(DialogInterface dialog, int item) {
                if (file.canRead()) {//注意，所有对文件的操作必须是在该文件可读的情况下才可以，否则报错
                    if (item == 0) {//复制
                        if (file.isFile() && "txt".equals((file.getName()
                                .substring(file.getName().lastIndexOf(".") + 1,
                                        file.getName().length())).toLowerCase())) {
                            Toast.makeText(getApplicationContext(), "已复制!", Toast.LENGTH_SHORT).show();
                            //复制标志位，表明已复制文件
                            isCopy = true;
                            //取得复制文件的名字
                            mCopyFileName = file.getName();
                            //记录复制文件的路径
                            mOldFilePath = mCurrentFilePath + java.io.File.separator + mCopyFileName;
                        } else {
                            Toast.makeText(getApplicationContext(), "对不起,目前只支持复制文本文件!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (item == 1) {//重命名
                        initRenameDialog(file);
                    } else if (item == 2) {//删除
                        initDeleteDialog(file);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "对不起，您的访问权限不足!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        //列表项名称
        String[] mMenu = {"复制", "重命名", "删除"};
        //显示操作选择对话框
        new AlertDialog.Builder(this)
                .setTitle("请选择操作!")
                .setItems(mMenu, listener)
                .setPositiveButton("取消", null).show();
    }


    EditText mET;
    //显示重命名对话框
    private void initRenameDialog(final File file) {
        LayoutInflater mLI = LayoutInflater.from(this);
        //初始化重命名对话框
        LinearLayout mLL = (LinearLayout) mLI.inflate(R.layout.rename_dialog, null);
        mET =  mLL.findViewById(R.id.new_filename2);
        //显示当前的文件名
        mET.setText(file.getName());
        //设置监听器
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String modifyName = mET.getText().toString();
                final String modifyFilePath = file.getParentFile().getPath() + java.io.File.separator;
                final String newFilePath = modifyFilePath + modifyName;
                //判断该新的文件名是否已经在当前目录下存在
                if (new File(newFilePath).exists()) {
                    if (!modifyName.equals(file.getName())) {//把“重命名”操作时没做任何修改的情况过滤掉
                        //弹出该新命名后的文件已经存在的提示，并提示接下来的操作
                        new AlertDialog.Builder(getApplicationContext())
                                .setTitle("提示!")
                                .setMessage("该文件名已存在，是否要覆盖?")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        file.renameTo(new File(newFilePath));
                                        Toast.makeText(getApplicationContext(),
                                                "the file path is " + new File(newFilePath), Toast.LENGTH_SHORT).show();
                                        //更新当前目录信息
                                        initFileListInfo(file.getParentFile().getPath());
                                    }
                                })
                                .setNegativeButton("取消", null).show();
                    }
                } else {
                    //文件名不重复时直接修改文件名后再次刷新列表
                    file.renameTo(new File(newFilePath));
                    initFileListInfo(file.getParentFile().getPath());
                }
            }

        };
        //显示对话框
        AlertDialog renameDialog = new AlertDialog.Builder(this).create();
        renameDialog.setView(mLL);
        renameDialog.setButton("确定", listener);
        renameDialog.setButton2("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //什么都不做，关闭当前对话框
            }
        });
        renameDialog.show();
    }

    //弹出删除文件/文件夹的对话框
    private void initDeleteDialog(final File file) {
        new AlertDialog.Builder(this)
                .setTitle("提示!")
                .setMessage("您确定要删除该" + (file.isDirectory() ? "文件夹" : "文件") + "吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.isFile()) {
                            //是文件则直接删除
                            file.delete();
                        } else {
                            //是文件夹则用这个方法删除
                            deleteFolder(file);
                        }
                        //重新遍历该文件的父目录
                        initFileListInfo(file.getParent());
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    //删除文件夹的方法（递归删除该文件夹下的所有文件）
    public void deleteFolder(File folder) {
        File[] fileArray = folder.listFiles();
        if (fileArray.length == 0) {
            //空文件夹则直接删除
            folder.delete();
        } else {
            //遍历该目录
            for (File currentFile : fileArray) {
                if (currentFile.exists() && currentFile.isFile()) {
                    //文件则直接删除
                    currentFile.delete();
                } else {
                    //递归删除
                    deleteFolder(currentFile);
                }
            }
            folder.delete();
        }
    }


    /**
     * 注册广播
     */
    private IntentFilter mFilter;
    private MainActivity2.FileBroadcast mFileBroadcast;
    private IntentFilter mIntentFilter;
    private SearchBroadCast mServiceBroadCast;
    @Override
    protected void onStart() {
        super.onStart();
        mFilter = new IntentFilter();
        mFilter.addAction(FileService.FILE_SEARCH_COMPLETED);
        mFilter.addAction(FileService.FILE_NOTIFICATION);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(KEYWORD_BROADCAST);
        if (mFileBroadcast == null) {
            mFileBroadcast = new MainActivity2.FileBroadcast();
        }
        if (mServiceBroadCast == null) {
            mServiceBroadCast = new SearchBroadCast();
        }
        this.registerReceiver(mFileBroadcast, mFilter);
        this.registerReceiver(mServiceBroadCast, mIntentFilter);
    }


    /**
     * 注销广播
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NullPointError", "onDestroy");
        mFileNames.clear();
        mFilePaths.clear();
        this.unregisterReceiver(mFileBroadcast);
        this.unregisterReceiver(mServiceBroadCast);
    }

    private String mAction;
    //搜索标志
    public static boolean isComeBackFromNotification = false;

    /**
     * 内部广播类
     */
    class FileBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAction = intent.getAction();
            // 搜索完毕的广播
            if (FileService.FILE_SEARCH_COMPLETED.equals(mAction)) {
                mFileNames = intent.getStringArrayListExtra("mFileNameList");
                mFilePaths = intent.getStringArrayListExtra("mFilePathsList");
                Toast.makeText(getApplicationContext(), "搜索完毕!", Toast.LENGTH_SHORT).show();
                //这里搜索完毕之后应该弹出一个弹出框提示用户要不要显示数据
                searchCompletedDialog("搜索完毕，是否马上显示结果?");
                getApplicationContext().stopService(serviceIntent);//当搜索完毕的时候停止服务，然后在服务中取消通知
                // 点击通知栏跳转过来的广播
            } else if (FileService.FILE_NOTIFICATION.equals(mAction)) {//点击通知回到当前Activity，读取其中信息
                String mNotification = intent.getStringExtra("notification");
                Toast.makeText(getApplicationContext(), mNotification, Toast.LENGTH_LONG).show();
                searchCompletedDialog("你确定要取消搜索吗?");
            }
        }
    }

    //搜索完毕和点击通知过来时的提示框
    private void searchCompletedDialog(String message) {
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //当弹出框时，需要对这个确定按钮进行一个判断，因为要对不同的情况做不同的处理（2种情况）
                        // 1.搜索完毕
                        // 2.取消搜索
                        if (FileService.FILE_SEARCH_COMPLETED.equals(mAction)) {
                            if (mFileNames.size() == 0) {
                                Toast.makeText(getApplicationContext(), "无相关文件/文件夹!", Toast.LENGTH_SHORT).show();
                            } else {
                                //更新数据
                                mAdapter1.upData(mFileNames, mFilePaths);
                                mAdapter1.setOnItemClickListener(MainActivity2.this);
                                mRv1.setAdapter(mAdapter1);
                            }
                        } else {
                            //设置搜索标志为true，
                            isComeBackFromNotification = true;
                            //关闭服务，取消搜索
                            getApplicationContext().stopService(serviceIntent);
                        }
                    }
                })
                .setNegativeButton("取消", null);
        searchDialog.create();
        searchDialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id==R.id.action_create){
            createFolder();
            return true;
        }else if (id==R.id.action_copy){
            palseFile();
            return true;
        }else if (id==R.id.action_search){
            searchDialog();
            return true;
        }else if (id==R.id.action_exit){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        /**每次点击一个Menu关闭DrawerLayout，方法为drawer.closeDrawer(GravityCompat.START);

         通过onBackPressed方法,当点击返回按钮的时候,如果DrawerLayout是打开状态则关闭*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
