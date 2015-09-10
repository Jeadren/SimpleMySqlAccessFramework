/**********************************************************************
 * 	MySqlHelper.java
 * 
 *  ��Ҫ��װ��MYSQL����������JDBC����
 *  �������ü���ʵ����һ�������ô�С�����ӳ�
 * 
 *  һ����˵��һ�����������ݿ��ѯ���̣�ֻ��Ҫ����һ��MySqlHelper��ʵ��
 *  ���ʵ����ThreadPoolPack���𴴽�����ThreadPoolPack�����̳߳�
 *  �û�һ�㲻Ӧ�ò������ֻ࣬��Ҫ����ThreadPoolPack
 *  
 **********************************************************************/

package pres.maxwell.nc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MySqlHelper {
	
	/* �����������ݿ� */
    private String url = "";
    private String userName = "";
    private String password = "";
    
    /* ���ӳ� */
	private ArrayList<Connection> connectionPool = new ArrayList<Connection>();
	private int connectionPoolSize ;				//���ӳ����������
	private boolean connectionPoolIdleArr[];		//���ӳؿ��б������,��Ϊtrue���ʾ��Ӧ�����е�Connection����
	
	/* �вι��캯�� */
	public MySqlHelper(String url,String userName,String password,int connectionPoolSize){
		
    	this.url = url;
    	this.userName = userName;
    	this.password = password;
    	
    	/* �������ӳ� */
    	this.connectionPoolSize = connectionPoolSize;
    	connectionPoolIdleArr = new boolean[connectionPoolSize];
    	
    	for(int i=0;i<connectionPoolIdleArr.length;i++){
    		connectionPoolIdleArr[i] = false;
    	}
    	
    }
    
    /* ֱ�������ȡ�µ�����,˽�з�������ֹ���������ӳ�ֱ�ӻ�ȡ�µ����� */
	private Connection getConnection() {
		
        Connection conn = null;		//����null��ʾ�������ɹ�
		
        try {
            conn = DriverManager.getConnection(url, userName, password);
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
		
        return conn;
    }
	

	/* �����ӳ��л�ȡ����,������ӳ�û�����ȡ�µ�����,���򷵻ؿ��е����� */
	public Connection getConnectionFromPool() {

		synchronized (connectionPool) {
			Connection conn = null;
			
			
			if(connectionPool.size()<connectionPoolSize){		//���ӳػ�û��
			
				/* ע������Ҫ����ͬ���� */
				synchronized (connectionPool) {
					conn = getConnection();
					if(conn!=null){
						connectionPool.add(conn);
						System.out.println("MySqlHelper����� " + Integer.toHexString(conn.hashCode()) + " �����ӳ�");
						return conn;
					}
				}
			}
			else{	//���ӳ�����
				
				/* �����̳߳ػ�߳���<=���ӳ������������һ���߳̿���һ�����ӣ������Ե��߳���Ҫ����ʱ�ض������ӿ��� */
		    	for(int i = 0;i < connectionPoolIdleArr.length;i++){
		    		if(connectionPoolIdleArr[i] == true){
		    			conn = (Connection)connectionPool.get(i);
		    			connectionPoolIdleArr[i] = false;
		    			return conn;
		    		}
		    	}	
			}
			return conn;
			
		}		
	}
	
	/* ��������״̬Ϊ���� */
	public void setConnectionStateToIdle(Connection conn) {

		synchronized (connectionPoolIdleArr) {
			
			int index = connectionPool.indexOf(conn);
			if(index == -1){
				throw new RuntimeException("MySqlHelper��connection���ڼ�����");			
			}
			else{
				connectionPoolIdleArr[index] = true;				
			}
		}		
	}	
	
	/* �ر����ӳ������е����� */	
	public void closeAllConnections() {
		
		synchronized (connectionPool) {
			
			for(Connection conn:connectionPool){
				
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				System.out.println("MySqlHelper���ر����� " + Integer.toHexString(conn.hashCode()));	
			}
			
			connectionPool.clear();
		}
		
		/* �������ӳؿ��б�������е����Ӳ�Ϊ���� */
		synchronized (connectionPoolIdleArr) {
			
	    	for(int i=0;i<connectionPoolIdleArr.length;i++){
	    		connectionPoolIdleArr[i] = false;
	    	}    	
		}	
	
	}	
	
	
	/* ִ�в�ѯSQL��䣬һ������ִ��Select SQL��� */		
    public ResultSet executeQuery(Connection conn, String sql, String... parameters) {
    	
        ResultSet rs = null;
        PreparedStatement ps = null;
        
        try {
        	
        	/* Ԥ�����ѯ��� */
            ps = conn.prepareStatement(sql);
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    ps.setString(i + 1, parameters[i]);
                }
            }
            
            rs = ps.executeQuery();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } 
        
        return rs;
    }
    
    
	/* ִ�и��²�����SQL��䣬������ִ��Update/Delete/Insert/Create SQL��� */		
    public Integer executeUpdate(Connection conn, String sql, String... parameters) {
    	
        PreparedStatement ps = null;
        Integer ret;
        
        try {
            ps = conn.prepareStatement(sql);

            /* Ԥ����SQL��� */
            if (parameters != null)
                for (int i = 0; i < parameters.length; i++) {
                    ps.setString(i + 1, parameters[i]);
                }
            
            ret = ps.executeUpdate();
        } 
        catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }  
        
        return ret;
    }
    
    
    /* ִ�ж����²�����SQL��䣬������ִ��Update/Delete/Insert��ϣ����������һ������ */	
    public void executeTransaction(Connection conn, String[] sql, String[]... parameters){
    	
    	PreparedStatement ps = null;
    	
        try {
            //ʹ���ӿ���ִ��һ������
            conn.setAutoCommit(false);
            
            for (int i = 0; i < sql.length; i++) {
            	
            	//Ԥ����SQL���
                if (parameters[i] != null) {
                    ps = conn.prepareStatement(sql[i]);
                  
                    for (int j = 0; j < parameters[i].length; j++)
                        ps.setString(j + 1, parameters[i][j]);
                }
                
                ps.executeUpdate();
            }
            
            //�ύ�����ӣ���ʱ�ſ�ʼִ��
            conn.commit();
            
        } 
        catch (Exception e) {
        	
        	System.out.println("MySqlHelper����������쳣��");
        	
            try {
            	//�����쳣�ع�����
                conn.rollback();
            } 
            catch (SQLException e1) {
                //e1.printStackTrace();
            	throw new RuntimeException("MySqlHelper������ع�ʧ�ܣ�");
            }
            
            //e.printStackTrace();
            System.out.println("MySqlHelper������ع��ɹ���");
            throw new RuntimeException(e.getMessage());
            
        } 
        
        System.out.println("MySqlHelper����������ִ����� ");	
    }
    
}
