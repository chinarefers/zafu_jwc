
package cn.edu.zafu.jwc.util;

import android.app.ProgressDialog;
import android.content.Context;


/**
 * ����������
 * @author lizhangqu
 * @date 2015-2-1
 */
public class CommonUtil {
	
	/**���ָ����Χ�ڵ������
	 * @param max
	 * @return int
	 */
	public static int getRandom(int max){
		return (int)(Math.random()*max);
	}
	
	/**������������һ�����������ģ��壬�����գ�ת��Ϊ��Ӧ�İ���������
	 * @param day 
	 * @return int
	 */
	public static int getDayOfWeek(String day) {
		if (day.equals("һ"))
			return 1;
		else if (day.equals("��"))
			return 2;
		else if (day.equals("��"))
			return 3;
		else if (day.equals("��"))
			return 4;
		else if (day.equals("��"))
			return 5;
		else if (day.equals("��"))
			return 6;
		else if (day.equals("��"))
			return 7;
		else
			return 0;
	}
	public static ProgressDialog getProcessDialog(Context context,String tips){
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setMessage(tips);
		dialog.setCancelable(false);
		return dialog;
	}
	
}
