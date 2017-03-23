/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitterdataanalysis;

import com.mongodb.BasicDBObject;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import static twitterdataanalysis.GetUserTimeline.isTableViewReady;

/**
 *
 * @author Tahmidolee
 */
public class UpdateLocalQueryTable {
    public static boolean isTableViewReady = false;
    public static boolean isUpdateTableViewReady = false;
    
    synchronized public static void updateInputViewTable(JTable searchTweetInputViewTable, BasicDBObject bObject) {

        DefaultTableModel defaultTableModel = (DefaultTableModel) searchTweetInputViewTable.getModel();
        
        searchTweetInputViewTable.setCellSelectionEnabled(false);
        searchTweetInputViewTable.setCellSelectionEnabled(true);
        
        if (isTableViewReady == false) {
            
            defaultTableModel.setRowCount(0);
            defaultTableModel.setColumnCount(bObject.size());
            Vector<Object> colVec = new Vector<>();
            for (int i = 0; i < bObject.toMap().keySet().toArray().length; i++) {
                colVec.add(bObject.toMap().keySet().toArray()[i]);
            }
            defaultTableModel.setColumnIdentifiers(colVec);
            isTableViewReady = true;
        }
        Vector<Object> rowVec = new Vector<>();
        for (int i = 0; i < bObject.toMap().values().toArray().length; i++) {
            rowVec.add(bObject.toMap().values().toArray()[i]);
        }
        
        defaultTableModel.addRow(rowVec);
    }
    
    synchronized public static void updateUpdateInputViewTable(JTable searchTweetInputViewTable, BasicDBObject bObject) {

        DefaultTableModel defaultTableModel = (DefaultTableModel) searchTweetInputViewTable.getModel();
        
        searchTweetInputViewTable.setCellSelectionEnabled(false);
        searchTweetInputViewTable.setCellSelectionEnabled(true);
        
        if (isUpdateTableViewReady == false) {
            
            defaultTableModel.setRowCount(0);
            defaultTableModel.setColumnCount(bObject.size());
            Vector<Object> colVec = new Vector<>();
            for (int i = 0; i < bObject.toMap().keySet().toArray().length; i++) {
                colVec.add(bObject.toMap().keySet().toArray()[i]);
            }
            defaultTableModel.setColumnIdentifiers(colVec);
            isUpdateTableViewReady = true;
        }
        Vector<Object> rowVec = new Vector<>();
        for (int i = 0; i < bObject.toMap().values().toArray().length; i++) {
            rowVec.add(bObject.toMap().values().toArray()[i]);
        }
        
        defaultTableModel.addRow(rowVec);
    }
}
