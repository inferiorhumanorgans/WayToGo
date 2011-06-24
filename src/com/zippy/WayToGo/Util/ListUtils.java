// Assume public domain
package com.zippy.WayToGo.Util;

import android.database.DatabaseUtils;
import java.util.Iterator;

/**
 *
 * @author http://snippets.dzone.com/posts/show/91
 */
public class ListUtils {
    //DatabaseUtils.appendEscapedSQLString
    public static String join( Iterable< ? extends Object > pColl, String separator ) {
        Iterator< ? extends Object > oIter;
        if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) )
            return "";
        StringBuilder oBuilder = new StringBuilder( String.valueOf( oIter.next() ) );
        while ( oIter.hasNext() )
            oBuilder.append( separator ).append( oIter.next() );
        return oBuilder.toString();
    }
    public static String sqlEscapedJoin( Iterable< ? extends Object > pColl, String separator ) {
        Iterator< ? extends Object > oIter;
        if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) )
            return "";
        StringBuilder oBuilder = new StringBuilder( DatabaseUtils.sqlEscapeString(String.valueOf( oIter.next() )) );
        while ( oIter.hasNext() )
            oBuilder.append( separator ).append( DatabaseUtils.sqlEscapeString(oIter.next().toString()) );
        return oBuilder.toString();
    }
}
