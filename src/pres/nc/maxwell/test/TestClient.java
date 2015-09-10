package pres.nc.maxwell.test;
/**********************************************************************
 * 	TestClient.java
 * 
 *  ��Ҫ��װһϵ�п�����JUnit���Եķ�������֤�����ȷ��
 *  
 *  �������¿�����JUnit���Եķ�����
 *  
 *  testSelectInSingleTask������SQL��ѯ������
 *  testSelectInMultiTask������SQL��ѯ������ÿ������һ���̣߳�
 *  
 *  testUpdateInSingleTask������SQL���²���Update������
 *  testInsertInSingleTask������SQL���²���Insert������ 
 *  testDeleteInSingleTask������SQL���²���Delete������
 *  
 *  testInsertInMultiTask������SQL���²���Insert������ÿ������һ���̣߳�
 *  testUpdateInMultiTask������SQL���²���Update������ÿ������һ���̣߳�
 *  testDeleteInMultiTask������SQL���²���Delete������ÿ������һ���̣߳� 
 *  
 *  testMixUpdateInMultiTask������SQL���²���Insert/Update/Delete�������ϣ�ÿ������һ���̣߳�
 *  
 *  testTransactionInSingleTask������SQL������������
 *  
 *  testCreateInSingleTask������SQL���²���Create������
 *  
 **********************************************************************/


import java.sql.ResultSet;
import java.util.concurrent.Future;
import org.junit.Test;

import pres.nc.maxwell.ThreadPoolPack;


public class TestClient {

	private final String SETTING_FILE =  "databaseSetting.properties";
	
	/* ����SQL��ѯ������ */
	@Test
	public void testSelectInSingleTask(){

		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ύ�����̳߳� */
		String sql = "SELECT * FROM userinfo";
		Future<ResultSet> future1 = tp.submitSqlQuery(sql);		
		
		/* ��ý��������ӡ */
		ResultSet rs = (ResultSet) tp.getResult(future1);
		tp.printResultSet(rs,"userName","password","gender","salary");
		tp.closeResultSet(rs);		

		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL��ѯ������ÿ������һ���̣߳� */
	@Test
	public void testSelectInMultiTask(){
		
		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ظ��ύ���� */
		int repeatTimes = 200;
		
		/* �������ڱ��治ͬ�̵߳õ��Ľ���� */
		@SuppressWarnings("unchecked")
		Future<ResultSet>[] future = new Future[repeatTimes];
		
		for(int i = 0;i < repeatTimes;i++){
			String sql = "SELECT * FROM userinfo";
			future[i] = tp.submitSqlQuery(sql);
		}
		
		/* ��ӡ�����
		 * ע������ʹ�õ��ж�������getCompletedTaskCount(),�����߳̿������ڱ��̳߳ؾܾ����޷�ִ�� */
		for(int i=0;i<tp.getCompletedTaskCount();i++){			
			ResultSet rs = (ResultSet) tp.getResult(future[i]);
			tp.printResultSet(rs,"userName","password","gender","salary");
			tp.closeResultSet(rs);
		}

		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Update������ */
	@Test
	public void testUpdateInSingleTask(){

		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ύ�����̳߳� */
		String sql = "UPDATE userinfo SET password=?,salary=? WHERE username = 'maxwell'";
        String[] parameters = { "nc", "888888.00" };
        Future<Integer> future = tp.submitSqlUpdate(sql,parameters);		
        
        tp.printUpdateResult(future);
		
		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Insert������ */
	@Test
	public void testInsertInSingleTask(){

		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ύ�����̳߳� */
		String sql = "INSERT INTO userinfo (username,password,gender,salary) VALUES (?,?,?,?)";
        String[] parameters = { "xiaoming", "123123", "male", "5000.00" };
        Future<Integer> future = tp.submitSqlUpdate(sql,parameters);		
        
        tp.printUpdateResult(future);
		
		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Delete������ */
	@Test
	public void testDeleteInSingleTask(){

		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ύ�����̳߳� */
		String sql = "DELETE FROM userinfo WHERE username = ?";
        String[] parameters = { "xiaoming" };
        Future<Integer> future = tp.submitSqlUpdate(sql,parameters);		
        
        tp.printUpdateResult(future);
		
		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Insert������ÿ������һ���̣߳� */
	@Test
	public void testInsertInMultiTask(){
		
		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ظ��ύ���� */
		int repeatTimes = 200;
		
		/* �������ڱ��治ͬ�̵߳õ��Ľ���� */
		@SuppressWarnings("unchecked")
		Future<Integer>[] future = new Future[repeatTimes];
		
		for(int i = 0;i < repeatTimes;i++){
			String sql = "INSERT INTO userinfo (username,password,gender,salary) VALUES (?,?,?,?)";
	        String[] parameters = { "xiaoming" + i, "123123", "male", "5000.00" };
			future[i] = tp.submitSqlUpdate(sql,parameters);	
		}
		
		/* ��ӡ�����
		 * ע������ʹ�õ��ж�������getCompletedTaskCount(),�����߳̿������ڱ��̳߳ؾܾ����޷�ִ�� */
		for(int i=0;i<tp.getCompletedTaskCount();i++){			
			tp.printUpdateResult(future[i]);
		}

		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Update������ÿ������һ���̣߳� */
	@Test
	public void testUpdateInMultiTask(){
		
		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ظ��ύ���� */
		int repeatTimes = 200;
		
		/* �������ڱ��治ͬ�̵߳õ��Ľ���� */
		@SuppressWarnings("unchecked")
		Future<Integer>[] future = new Future[repeatTimes];
		
		for(int i = 0;i < repeatTimes;i++){
			String sql = "UPDATE userinfo SET password=?,salary=? WHERE username = 'xiaoming" + i + "'";
	        String[] parameters = { "ceshi", "6500.00" };
			future[i] = tp.submitSqlUpdate(sql,parameters);	
		}
		
		/* ��ӡ�����
		 * ע������ʹ�õ��ж�������getCompletedTaskCount(),�����߳̿������ڱ��̳߳ؾܾ����޷�ִ�� */
		for(int i=0;i<tp.getCompletedTaskCount();i++){			
			tp.printUpdateResult(future[i]);
		}

		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Delete������ÿ������һ���̣߳� */
	@Test
	public void testDeleteInMultiTask(){
		
		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ظ��ύ���� */
		int repeatTimes = 200;
		
		/* �������ڱ��治ͬ�̵߳õ��Ľ���� */
		@SuppressWarnings("unchecked")
		Future<Integer>[] future = new Future[repeatTimes];
		
		for(int i = 0;i < repeatTimes;i++){
			String sql = "DELETE FROM userinfo WHERE username = ?";
	        String[] parameters = { "xiaoming" + i };
			future[i] = tp.submitSqlUpdate(sql,parameters);	
		}
		
		/* ��ӡ�����
		 * ע������ʹ�õ��ж�������getCompletedTaskCount(),�����߳̿������ڱ��̳߳ؾܾ����޷�ִ�� */
		for(int i=0;i<tp.getCompletedTaskCount();i++){			
			tp.printUpdateResult(future[i]);
		}

		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Insert/Update/Delete�������ϣ�ÿ������һ���̣߳�
	 * ע��Ҫ������ͬ�ļ�¼�������ͻ�׳��쳣
	 * */
	@Test
	public void testMixUpdateInMultiTask(){
		
		/* ׼�����ݣ���ֹ���ݲ������׳��쳣 */
		testDeleteInMultiTask();
		testInsertInMultiTask();
		
		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ظ��ύ���� */
		int repeatTimes = 30;
		
		/* �������ڱ��治ͬ�̵߳õ��Ľ���� */
		@SuppressWarnings("unchecked")
		Future<Integer>[] future = new Future[repeatTimes*3];
		
		for(int i = 0;i < repeatTimes;i++){
			String sql = "INSERT INTO userinfo (username,password,gender,salary) VALUES (?,?,?,?)";
	        String[] parameters = { "daming" + i, "123123", "male", "5000.00" };
			future[i] = tp.submitSqlUpdate(sql,parameters);	
		}
		
		for(int i = repeatTimes;i < repeatTimes*2 ;i++){
			String sql = "UPDATE userinfo SET password=?,salary=? WHERE username = 'xiaoming" + i + "'";
	        String[] parameters = { "ceshiMix", "6500.00" };
			future[i] = tp.submitSqlUpdate(sql,parameters);	
		}
		for(int i = repeatTimes*2 ;i < repeatTimes*3 ;i++){
			String sql = "DELETE FROM userinfo WHERE username = ?";
	        String[] parameters = { "xiaoming" + i };
			future[i] = tp.submitSqlUpdate(sql,parameters);	
		}
	
		/* ��ӡ�����
		 * ע������ʹ�õ��ж�������getCompletedTaskCount(),�����߳̿������ڱ��̳߳ؾܾ����޷�ִ�� */
		for(int i=0;i<tp.getCompletedTaskCount();i++){			
			tp.printUpdateResult(future[i]);
		}

		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL������������ */
	@Test
	public void testTransactionInSingleTask(){

		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ύ�����̳߳� */
		String sql1 = "UPDATE userinfo SET salary=salary-1000 WHERE username = ?";
        String sql2 = "UPDATE userinfo SET salary=salary+1000000 WHERE username = ?";
        String[] sql = { sql1, sql2 };
        
        String[] sql1_params = { "xiaomi" };
        String[] sql2_params = { "fgn" };
        String[][] parameters = { sql1_params, sql2_params };
        
		tp.submitSqlUpdateTransaction(sql,parameters);		

		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
	
	
	/* ����SQL���²���Create������ */
	@Test
	public void testCreateInSingleTask(){

		/* ���������ļ����̳߳ػ�߳������̳߳�����߳��� == ���ӳ������������ */
		ThreadPoolPack tp = new ThreadPoolPack(SETTING_FILE,5);
		
		/*�����̳߳أ�����߳���Ϊ1�������̻߳ʱ�䣨long��0*/
		tp.createThreadPool(20, 0L);
		
		/* �ύ�����̳߳� */
		String sql = "CREATE TABLE `testTable` ("
					  +"`id` int(11) NOT NULL AUTO_INCREMENT,"
					  +"`username` varchar(13) NOT NULL,"
					  +"`password` varchar(13) NOT NULL,"
					  +"`gender` varchar(6) DEFAULT NULL,"
					  +"`salary` double(8,2) DEFAULT NULL,"
					  +"PRIMARY KEY (`id`),"
					  +"UNIQUE KEY `UNIQUE` (`username`));";
        
        Future<Integer> future = tp.submitSqlUpdate(sql);		
        
        tp.printUpdateResult(future);
		
		tp.shutdownThreadPool();
		tp.closeAllConnections();
	}
}
