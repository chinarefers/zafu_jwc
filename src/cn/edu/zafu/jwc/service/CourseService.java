package cn.edu.zafu.jwc.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import cn.edu.zafu.jwc.model.Course;
import cn.edu.zafu.jwc.util.CommonUtil;

/**
 * Course���ҵ���߼�����
 * 
 * @author lizhangqu
 * @date 2015-2-1
 */
public class CourseService {
	/**
	 * ����һ�ڿγ�
	 * 
	 * @param course
	 * @return
	 */
	public boolean save(Course course) {
		return course.save();
	}

	/**
	 * ��ѯ���пγ�
	 * 
	 * @return
	 */
	public List<Course> findAll() {
		return DataSupport.findAll(Course.class);
	}

	/**
	 * ������ҳ���ؽ�������γ̲�����
	 * 
	 * @param content
	 * @return
	 */
	public String parseCourse(String content) {
		StringBuilder result = new StringBuilder();
		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("table#Table1");
		Element element = elements.get(0).child(0);
		//�Ƴ�һЩ��������
		element.child(1).remove();
		element.child(1).child(0).remove();
		element.child(5).child(0).remove();
		element.child(9).child(0).remove();
		int rowNum = element.childNodeSize() - 1;
		
		for (int i = 1; i < rowNum; i++) {
			Element row = element.child(i);
			int columnNum = row.childNodeSize() - 2;
			for (int j = 1; j < columnNum; j++) {
				Element column = row.child(j);
				
				if (!column.html().equals("&nbsp;")) {
					result.append(column.html()+ "\n\n");
					splitCourse(column.html());//����ȡ�γ�������ܰ�����ڿΣ����зָ�
				}
			}
		}
		return result.toString();
	}

	/**
	 * ���õ�˫��
	 * @param week
	 * @param course
	 */
	public void setEveryWeekByChinese(String week, Course course) {
		// 1�����ܣ�2����˫��
		if (week != null) {
			if (week.equals("����"))
				course.setEveryWeek(1);
			else if (week.equals("˫��"))
				course.setEveryWeek(2);
		}
		// Ĭ��ֵΪ0������ÿ��
	}

	/**
	 * ���ݴ������Ŀγ̸�ʽת��Ϊ��Ӧ��ʵ���ಢ����
	 * @param sub
	 * @return
	 */
	private Course storeCourseByResult(String sub) {
		//�ܶ���1,2��{��4-16��}		��,1,2,4,16,null
		//{��2-10��|3��/��}		null,null,null,2,10,3��/��
		//�ܶ���1,2��{��4-16��|˫��}	��,1,2,4,16,˫��
		//�ܶ���1��{��4-16��}		��,1,null,4,16,null
		//�ܶ���1��{��4-16��|˫��}	��,1,null,4,16,˫��
		// str��ʽ���ϣ�����ֻ�Ǽ򵥿���ÿ���ζ�ֻ�����ڿΣ�ʵ���������ں��Ľڣ�ģʽ��Ҫ�Ķ�������ƥ��ģʽ�������޸�
		// String reg="��.��(\\d{1,2}),(\\d{1,2})��\\{��(\\d{1,2})-(\\d{1,2})��\\}";
		//String reg = "��(.)��(\\d{1,2}),(\\d{1,2})��\\{��(\\d{1,2})-(\\d{1,2})��\\|?((.��))?\\}";
		String reg = "��?(.)?��?(\\d{1,2})?,?(\\d{1,2})?��?\\{��(\\d{1,2})-(\\d{1,2})��\\|?((.*��))?\\}";
		
		String splitPattern = "<br />";
		System.out.println(sub);
		String[] temp = sub.split(splitPattern);
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(temp[1]);
		matcher.matches();
		Course course = new Course();
		course.setCourseName(temp[0]);
		course.setCourseTime(temp[1]);
		course.setTeacher(temp[2]);
		try{
			//�������Խ�磬��û�н�ʦ
			course.setClasssroom(temp[3]);
		}catch(ArrayIndexOutOfBoundsException e){
			course.setClasssroom("�޽�ʦ");
		}
		System.out.println(temp[1]);
		course.setDayOfWeek(CommonUtil.getDayOfWeek(matcher.group(1)));
		course.setStartSection(Integer.parseInt(matcher.group(2)));
		if(null!=matcher.group(3))
			course.setEndSection(Integer.parseInt(matcher.group(3)));
		else
			course.setEndSection(Integer.parseInt(matcher.group(2)));
		course.setStartWeek(Integer.parseInt(matcher.group(4)));
		course.setEndWeek(Integer.parseInt(matcher.group(5)));
		String t = matcher.group(6);
		setEveryWeekByChinese(t, course);
		save(course);
		return course;
	}

	/**
	 * 
	 * ��ȡ�γ̸�ʽ�����ܰ�����ڿ�
	 * @param str
	 * @return
	 */
	private int splitCourse(String str) {
		String pattern = "<br /><br />";
		String[] split = str.split(pattern);
		if (split.length > 1) {// �������һ�ڿ�
			for (int i = 0; i < split.length; i++) {
				if(!(split[i].startsWith("<br />")&&split[i].endsWith("<br />"))){
					storeCourseByResult(split[i]);//���浥�ڿ�
				}
				else{
					//<br />�Ļ���������γ̣�<br />���յ�10��{��17-17��}<br />���ΰ<br />
					//���ϸ�ʽ�����⴦��
					int brLength="<br />".length();
					String substring = split[i].substring(brLength, split[i].length()-brLength);
					storeCourseByResult(substring);//���浥�ڿ�
				}
			}
			return split.length;
		} else {
			storeCourseByResult(str);//����
			return 1;
		}
	}
}
