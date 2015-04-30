package cn.edu.zafu.jwc.activity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.litepal.tablemanager.Connector;

import cn.edu.zafu.jwc.application.JWCApplication;
import cn.edu.zafu.jwc.service.CourseService;
import cn.edu.zafu.jwc.service.LinkService;
import cn.edu.zafu.jwc.util.CommonUtil;
import cn.edu.zafu.jwc.util.HttpUtil;
import cn.edu.zafu.jwc.util.SharedPreferenceUtil;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private EditText username, password, secrectCode;// �˺ţ����룬��֤��
	private ImageView code;// ��֤��
	private Button  flashCode, login;//ˢ����֤�룬��¼
	private PersistentCookieStore cookie;
	private SQLiteDatabase db;
	private LinkService linkService;
	private CourseService courseService;
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.getCode:
				getCode();
				break;
			case R.id.login:
				login();
				break;
			}

		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initValue();//������ʼ��
		initView();//��ͼ��ʼ��
		initEvent();// �¼���ʼ��
		initCookie(this);// cookie��ʼ��
		initDatabase();// ���ݿ��ʼ��
	}
	private void initValue() {
		JWCApplication application=((JWCApplication)getApplicationContext());
		linkService=application.getLinkService();
		courseService=application.getCourseService();
	}
	/**
	 * ��ʼ��View
	 */
	private void initView() {
		secrectCode = (EditText) findViewById(R.id.secrectCode);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		flashCode = (Button) findViewById(R.id.getCode);
		login = (Button) findViewById(R.id.login);
		code = (ImageView) findViewById(R.id.codeImage);
	}
	/**
	 * ��ʼ�¼�
	 */
	private void initEvent() {
		// һЩ�е���¼��ĳ�ʼ��
		flashCode.setOnClickListener(listener);
		login.setOnClickListener(listener);
	}
	/**
	 * ��ʼ�����ݿ�
	 */
	private void initDatabase() {
		db = Connector.getDatabase();
		// ��assetsĿ¼�µ�litepal.xml���������ݿ������汾��ӳ���ϵ
	}

	/**
	 * ��ʼ��Cookie
	 */
	private void initCookie(Context context) {
		//����������ǰ��ʼ��
		cookie = new PersistentCookieStore(context);
		HttpUtil.getClient().setCookieStore(cookie);
	}
	private void jump2Main() {
		SharedPreferenceUtil util=new SharedPreferenceUtil(getApplicationContext(), "accountInfo");
		util.setKeyData("username", HttpUtil.txtUserName);
		util.setKeyData("password", HttpUtil.TextBox2);
		util.setKeyData("isLogin", "TRUE");
		Intent intent=new Intent(LoginActivity.this,MainActivity.class);
		startActivity(intent);
		finish();
	}
	/**
	 * ��¼
	 */
	private void login() {
		HttpUtil.txtUserName = username.getText().toString().trim();
		HttpUtil.TextBox2 = password.getText().toString().trim();
		//��Ҫʱ����֤��ע��
		//HttpUtil.txtSecretCode = secrectCode.getText().toString().trim();
		if (TextUtils.isEmpty(HttpUtil.txtUserName)
				|| TextUtils.isEmpty(HttpUtil.TextBox2)) {
			Toast.makeText(getApplicationContext(), "�˺Ż������벻��Ϊ��!",
					Toast.LENGTH_SHORT).show();
			return;
		}
		final ProgressDialog dialog =CommonUtil.getProcessDialog(LoginActivity.this,"���ڵ�¼�У�����");
		dialog.show();
		RequestParams params = HttpUtil.getLoginRequestParams();// ����������
		HttpUtil.URL_MAIN = HttpUtil.URL_MAIN.replace("XH",
				HttpUtil.txtUserName);// ��������ַ
		HttpUtil.getClient().setURLEncodingEnabled(true);
		HttpUtil.post(HttpUtil.URL_LOGIN, params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						try {
							String resultContent = new String(arg2, "gb2312");
							if(linkService.isLogin(resultContent)!=null){
								String ret = linkService.parseMenu(resultContent);
								Log.d("zafu", "login success:"+ret);
								Toast.makeText(getApplicationContext(),
										"��¼�ɹ�������", Toast.LENGTH_SHORT).show();
								jump2Main();
								
							}else{
								Toast.makeText(getApplicationContext(),"�˺Ż���������󣡣���", Toast.LENGTH_SHORT).show();
							}

						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} finally {
							dialog.dismiss();
						}
					}
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(getApplicationContext(), "��¼ʧ�ܣ�������",
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});
	}

	/**
	 * �����֤��
	 */
	private void getCode() {
		final ProgressDialog dialog =CommonUtil.getProcessDialog(LoginActivity.this,"���ڻ�ȡ��֤��");
		dialog.show();
		HttpUtil.get(HttpUtil.URL_CODE, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				
				InputStream is = new ByteArrayInputStream(arg2);
				Bitmap decodeStream = BitmapFactory.decodeStream(is);
				code.setImageBitmap(decodeStream);
				Toast.makeText(getApplicationContext(), "��֤���ȡ�ɹ�������",Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				
				Toast.makeText(getApplicationContext(), "��֤���ȡʧ�ܣ�����",
						Toast.LENGTH_SHORT).show();
				dialog.dismiss();

			}
		});
	}
}
