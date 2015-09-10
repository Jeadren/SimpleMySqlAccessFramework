/**********************************************************************
 * 	ThreadPoolPack.java
 * 
 *  public ThreadPoolPack�ࣺ��Ҫ��װ�̳߳أ��Ͳ���MySqlHelperʵ���ķ���
 *  default ExecuteQueryThread�ࣺ��װ��ѯSQL������
 *  default ExecuteUpdateThread�ࣺ��װ�����²�����SQL������
 *  default CommitTransactionThread�ࣺ��װ�����²�����SQL�������
 *  
 **********************************************************************/

package pres.maxwell.nc;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolPack {
	
	/* �������룺Java�������Ż�����������ôһ���������������ʽ����ԶΪfalse����䣬�������������������ǵĴ���������ֽ��롣 
	 * REFUSE_WHEN_FULL���Ƶ������߳�ʱ�̳߳ض�������ʱ�Ƿ�ܾ���trueΪ�ܾ���falseΪ�ȴ��̳߳ؿ���  */
	private final boolean REFUSE_WHEN_FULL = false;  
	
	private ThreadPoolExecutor executor;
	private int threadPoolCoreSize;
	
	/* һ����˵��һ���̳߳�ֻ��Ҫһ��MySqlHelper���ʵ�����������һ�����ӳأ����Դ���������� */
	private MySqlHelper mysqlHelperObj = null;

	
	/* ���췽��������MySqlHelperʵ�� */
	public ThreadPoolPack(String filePath,int threadPoolCoreSize){
				
		if(0 == threadPoolCoreSize){
			throw new RuntimeException("ThreadPoolPack�������̳߳���Сִ���߳�Ϊ1");
		}
		
		String url = "";
		String userName = "";
		String password = "";
		
		FileInputStream fis = null;
		
        try {
            /* �������ļ��ж�ȡ������Ϣ */
        	Properties pp = new Properties();
        	fis = new FileInputStream(filePath);
            pp.load(fis);
            url = pp.getProperty("url");
            userName = pp.getProperty("userName");
            password = pp.getProperty("password");

        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally {
            if (fis != null){
            	try {
                    fis.close();
                } 
            	catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        this.threadPoolCoreSize = threadPoolCoreSize;
		this.mysqlHelperObj = new MySqlHelper(url, userName, password, threadPoolCoreSize);	
	}

	
	/* �����̳߳أ�maxPoolSize���̳߳������߳���������̣߳���keepAliveTime���߳̿���ʱ����ʱ�䣨���룩 */
	public void createThreadPool(int maxPoolSize,long keepAliveTime) {
		
		if(maxPoolSize <= this.threadPoolCoreSize){
			throw new RuntimeException("ThreadPoolPack���̳߳�����߳�����������ӳ�����Ծ��������");
		}
		else{	
			executor = new ThreadPoolExecutor(
					this.threadPoolCoreSize,
					maxPoolSize,
					keepAliveTime,
					TimeUnit.MILLISECONDS,
					new ArrayBlockingQueue<Runnable>(maxPoolSize - this.threadPoolCoreSize));//�̳߳ػ������ = ����߳��� - ����߳���
		}
	}

	
	/* �ر��������� */
	public void closeAllConnections(){
		
		if(executor.isShutdown()==false){
			System.out.println("ThreadPoolPack�����棺�̳߳ػ�û�رգ����ܹر����ӳ�");
			return;		
		}
		
		while(executor.getPoolSize()!=0){
			//�ȴ����з��߳��˳�
		}
		
		mysqlHelperObj.closeAllConnections();	
	}
	
	
	/* �ر�ָ���Ľ���� */
	public void closeResultSet(ResultSet rs){
		
		try {
			rs.close();
			System.out.println("ThreadPoolPack���رս���� "+ Integer.toHexString(rs.hashCode()) );
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/* �ж��̳߳��Ƿ�ﵽ����߳��� */
	public boolean isThreadPoolFull(){
		
		int currentPoolSize = executor.getPoolSize()+ executor.getQueue().size();
		if(currentPoolSize < executor.getMaximumPoolSize() ){
			return false;
		}
		else{
			return true;
		}
	}
	
	
	/* �ر��̳߳أ����ڽ����µ��߳��ύ���󣬲�����ֹ���ڶ����л��߻�е��߳� */
	public void shutdownThreadPool(){
		
		executor.shutdown();
	}

	
	/* �����̳߳��Ѿ���ɵ������� */
	public long getCompletedTaskCount(){
	
		return executor.getCompletedTaskCount();
	}	
	
	
	/* ����̳߳��Ƿ������������߳� */
	public boolean checkThreadPool(){
		
		if(REFUSE_WHEN_FULL){
			if(isThreadPoolFull()==true){		
				System.out.println("ThreadPoolPack�������������������󱻾ܾ���");
				return false;
			}
		}
		
		if(!REFUSE_WHEN_FULL){
			
			while(isThreadPoolFull()==true){
				//�����������ȴ��Ķ��п���
			}
		}
		return true;
	}
	
	
	/* �ύ��ѯSQL����̣߳����ص��Ƿ�װ */
	public Future<ResultSet> submitSqlQuery(String sql,String... parameters) {
		
		if(!checkThreadPool()){
			return null;
		}

		/* �����߳��ಢ�ύ���̳߳� */
		ExecuteQueryThread queryThread = new ExecuteQueryThread(mysqlHelperObj,sql,parameters);	
		Future<ResultSet> future = executor.submit(queryThread);
		
		return future;
	}
	
	
	/* ��ý���� */
	public <V> Object getResult(Future<V> future) {
		
		Object rs = null;
		
		try {
			/* get����������ֱ�����̷߳��ؽ���� */
			rs = future.get();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	
		return rs;	
	}

	
	/* ��ӡ����� */
	public void printResultSet(ResultSet rs,String... args){
		
		try {	
			while (rs.next()) {
				for(int i=0;i<args.length;i++){	
					System.out.println(args[i]+":" + rs.getString(args[i]));
				}	
				
				System.out.println();
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}	
	}
		 
	
	/* �ύ���²���SQL����߳� */
	public Future<Integer> submitSqlUpdate(String sql,String... parameters) {
		
		if(!checkThreadPool()){
			return null;
		}

		/* �����߳��ಢ�ύ���̳߳� */
		ExecuteUpdateThread updateThread = new ExecuteUpdateThread(mysqlHelperObj,sql,parameters);	
		Future<Integer> future = executor.submit(updateThread);
		
		return future;
	}
	
	
	/* ��ӡ���²����Ľ�� */
	public void printUpdateResult(Future<Integer> future){
		
		Integer rs = (Integer) getResult(future);
		System.out.println("ThreadPoolPack�������ɹ�����Ӱ�������ݿ�������Ϊ " + rs.intValue() + " ��");	
	}	
	
	
	/* �ύ���²���SQL�����߳� */
	public void submitSqlUpdateTransaction(String[] sql,String[]... parameters) {
		
		if(!checkThreadPool()){
			return;
		}

		/* �����߳��ಢ�ύ���̳߳� */
		CommitTransactionThread transactionThread = new CommitTransactionThread(mysqlHelperObj,sql,parameters);	
		executor.submit(transactionThread);

	}	
	
	
	
	
	
}//TreadPoolPack




class ExecuteQueryThread implements Callable<ResultSet> {
	
	private Connection conn;
	private String sql = "";
	private String[] parameters;
	private ResultSet rs;
	
	private MySqlHelper mysqlHelperObj;
	
	
	/* ���췽�������Ҫִ�е�SQL���Ͳ�����MySqlHelper���� */
	public ExecuteQueryThread(MySqlHelper mysqlHelperObj,String sql,String... parameters){
		this.sql = sql;
		this.parameters = parameters;
		this.mysqlHelperObj = mysqlHelperObj;
	}	

	/* ���̳߳��л�ȡ���� */
	private void getConnection(){
		conn = mysqlHelperObj.getConnectionFromPool();
		
		/* ��ȡ����ʧ�ܣ����»�ȡ */
		while(conn == null){
			conn = mysqlHelperObj.getConnectionFromPool();
		}
		
		System.out.println("ThreadPoolPack���������"+ Integer.toHexString(conn.hashCode()));
	}
	
	@Override
	public ResultSet call() {	

		getConnection();
		
		try {
			rs = mysqlHelperObj.executeQuery(conn,sql,parameters);		
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			/* ��������Ϊ����״̬ */
			mysqlHelperObj.setConnectionStateToIdle(conn);
		}

		return rs;
	}

}//ExecuteQueryThread



class ExecuteUpdateThread implements Callable<Integer>{
	
	private Connection conn;
	private String sql = "";
	private String[] parameters;
	private MySqlHelper mysqlHelperObj;
	
	
	/* ���췽�������Ҫִ�е�SQL���Ͳ�����MySqlHelper���� */
	public ExecuteUpdateThread(MySqlHelper mysqlHelperObj,String sql,String... parameters){
		this.sql = sql;
		this.parameters = parameters;
		this.mysqlHelperObj = mysqlHelperObj;
	}	

	/* ���̳߳��л�ȡ���� */
	private void getConnection(){
		conn = mysqlHelperObj.getConnectionFromPool();
		
		/* ��ȡ����ʧ�ܣ����»�ȡ */
		while(conn == null){
			conn = mysqlHelperObj.getConnectionFromPool();
		}
		
		System.out.println("ThreadPoolPack���������"+ Integer.toHexString(conn.hashCode()));
	}
	
	@Override
	public Integer call() {	

		getConnection();
		
		Integer ret = null;
		try {	
			ret = mysqlHelperObj.executeUpdate(conn,sql, parameters);
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			/* ��������Ϊ����״̬ */
			mysqlHelperObj.setConnectionStateToIdle(conn);
		}
		
		return ret;
	}
	
	
}//ExecuteUpdateThread



class CommitTransactionThread implements Runnable{
	
	private Connection conn;
	private String[] sql;
	private String[][] parameters;
	private MySqlHelper mysqlHelperObj;
	
	
	/* ���췽�������Ҫִ�е�SQL���Ͳ�����MySqlHelper���� */
	public CommitTransactionThread(MySqlHelper mysqlHelperObj,String[] sql,String[]... parameters){
		this.sql = sql;
		this.parameters = parameters;
		this.mysqlHelperObj = mysqlHelperObj;
	}	

	/* ���̳߳��л�ȡ���� */
	private void getConnection(){
		conn = mysqlHelperObj.getConnectionFromPool();
		
		/* ��ȡ����ʧ�ܣ����»�ȡ */
		while(conn == null){
			conn = mysqlHelperObj.getConnectionFromPool();
		}
		
		System.out.println("ThreadPoolPack���������"+ Integer.toHexString(conn.hashCode()));
	}
	
	@Override
	public void run() {	

		getConnection();
		
		try {	
			mysqlHelperObj.executeTransaction(conn,sql, parameters);
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			/* ��������Ϊ����״̬ */
			mysqlHelperObj.setConnectionStateToIdle(conn);
		}
		
		
	}
	
	
}//CommitTransactionThread


