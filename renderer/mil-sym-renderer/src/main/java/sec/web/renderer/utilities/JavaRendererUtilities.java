package sec.web.renderer.utilities;

import ArmyC2.C2SD.Rendering.JavaRenderer;
import ArmyC2.C2SD.Rendering.TacticalGraphicIconRenderer;
import ArmyC2.C2SD.Utilities.ErrorLogger;
import ArmyC2.C2SD.Utilities.MilStdAttributes;
import ArmyC2.C2SD.Utilities.MilStdSymbol;
import ArmyC2.C2SD.Utilities.ModifiersUnits;
//import ArmyC2.C2SD.Utilities.MilStdAttributes;
import ArmyC2.C2SD.Utilities.RendererSettings;
import ArmyC2.C2SD.Utilities.SymbolDef;
import ArmyC2.C2SD.Utilities.SymbolDefTable;
import ArmyC2.C2SD.Utilities.SymbolUtilities;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import sec.web.json.utilities.JSONArray;
import sec.web.json.utilities.JSONException;
import sec.web.json.utilities.JSONObject;

/**
 *
 * @author stephen.pinizzotto
 */
@SuppressWarnings("unused")
public class JavaRendererUtilities {
    
    public static String HOSTILE_FILL_COLOR = "FFFF8080";
    public static String FRIENDLY_FILL_COLOR = "FF80E0FF";
    public static String NEUTRAL_FILL_COLOR = "FFAAFFAA";    
    public static String UNKNOWN_FILL_COLOR = "FFFFFF80";
    
    /**
     * Returns the default MIL-STD-2525C fill color for a symbol
     * code in ARGB format.  The string is formated AARRGGBB in
     * hex values.
     * 
     * @param symbolCode A 15 character MIL-STD-2525C symbol ID
     * @return the default "light" color of the symbol as specified
     * by the MIL-STD-2525C.  Returns null if it does not recognize
     * the affiliation.
     */
//    public static String getAffiliationFillColor(String symbolCode)
//    {
//        String color; 
//        
//        //if no color has been assigned use black, although, we should
//        // probably check affiliation here.
//        String affiliation = SymbolUtilities.getAffiliation(symbolCode);
//        if (affiliation.equals("F") || affiliation.equals("M") || 
//                affiliation.equals("D") || affiliation.equals("M"))                        
//        {
//            color = JavaRendererUtilities.FRIENDLY_FILL_COLOR;
//        }
//        else if (affiliation.equals("H") || affiliation.equals("S") || 
//                affiliation.equals("J") || affiliation.equals("K"))
//        {
//            color = JavaRendererUtilities.HOSTILE_FILL_COLOR;
//        }
//        else if (affiliation.equals("N") || affiliation.equals("L"))
//        {   
//            color = JavaRendererUtilities.NEUTRAL_FILL_COLOR;
//        }
//        else if (affiliation.equals("U") || affiliation.equals("P") || 
//                affiliation.equals("G") || affiliation.equals("W"))
//        {
//            color = JavaRendererUtilities.UNKNOWN_FILL_COLOR;
//        }
//        else
//        {   
//            color = null;
//        }                    
//        
//        return color;
//    }
    
    /**
     * Converts ARGB string format to the Google used ABGR string
     * format.  Google reverses the blue and red positioning.
     * @param rgbString A color string of the format AARRGGBB in hex value.
     * @return the reverse of the input string in hex.  The format should now be 
     * AABBGGRR
     */
    public static String ARGBtoABGR(String rgbString) {
        char[] c = rgbString.toCharArray();

        char temp1 = c[2];
        char temp2 = c[3];
        c[2] = c[6];
        c[3] = c[7];
        c[6] = temp1;
        c[7] = temp2;

        String bgrString = new String(c);

        return bgrString;
    }
    
    /**
     * Returns a symbolId with just the identifiable symbol Id
     * pieces.  All variable information is returned as '*'.  For
     * example, a boundary, "GFGPGLB----KUSX" returns "G*G*GLB---****X";
     * @param symbolCode A 15 character symbol ID.
     * @return The normalized SymbolCode.
     */
    public static String normalizeSymbolCode(String symbolCode) {
        
        String newSymbolCode = symbolCode;
        
        
        if (symbolCode.startsWith("G") || symbolCode.startsWith("S"))
        {
            // Remove Affiliation
            newSymbolCode = newSymbolCode.substring(0, 1) + '*' + newSymbolCode.substring(2);
            // Remove planned/present field
            newSymbolCode = newSymbolCode.substring(0, 3) + '*' + newSymbolCode.substring(4);
            // Remove echelon, special code and country codes
            newSymbolCode = newSymbolCode.substring(0, 10) + "****" + newSymbolCode.substring(14);
        }
                
        // If a unit replace last character with *.
        if (symbolCode.startsWith("S"))
        {
            newSymbolCode = newSymbolCode.substring(0, 14) + '*';
        }
        
        return newSymbolCode;
    }
    
    /**
     * 
     * @param SymbolInfo something like "SymbolID?LineColor=0x000000&FillColor=0xFFFFFF&size=35"
     */
    public static Map<String,String> createParameterMapFromURL(String SymbolInfo)
    {
        Map<String, String> modifiers = new HashMap<String, String>();
        String symbolID = null;
        String parameters = null;
        String key = null;
        String value = null;
        String arrParameters[] = null;
        String arrKeyValue[] = null;
        String temp = null;
        int questionIndex = SymbolInfo.lastIndexOf('?');
        
        try
        {
            if(questionIndex == -1)
                symbolID = java.net.URLDecoder.decode(SymbolInfo, "UTF-8");
            else
                 symbolID = java.net.URLDecoder.decode(SymbolInfo.substring(0, questionIndex), "UTF-8");
            
            //modifiers.put("SYMBOLID", symbolID);
        }
        catch(Exception exc)
        {
            System.err.println("Error parsing SymbolID");
            System.err.println(exc.getMessage());
        }

        try
        {   //build a map for the other createMilstdSymbol function to use
            //to build a milstd symbol.
            if(questionIndex > 0 && (questionIndex + 1 < SymbolInfo.length()))
            {
                parameters = SymbolInfo.substring(questionIndex + 1,SymbolInfo.length());
                arrParameters = parameters.split("&");

                for(int i = 0; i < arrParameters.length; i++)
                {
                    arrKeyValue = arrParameters[i].split("=");
                    if(arrKeyValue.length == 2 && arrKeyValue[1]!= null && arrKeyValue[1].equals("")==false)
                    {

                        key = arrKeyValue[0];
                        value = arrKeyValue[1];

                        temp = java.net.URLDecoder.decode(value, "UTF-8");
                        modifiers.put(key.toUpperCase(), temp);

                        //System.out.println("key: " + key + " value: " + temp);
                    }
                }
            }
        }
        catch(Exception exc)
        {
            System.err.println("Error parsing \"" + key.toUpperCase() + "\" parameter from URL");
            System.err.println(exc.getMessage());
        }
        return modifiers;
    }
            /**
         * Takes a string and parses information to build a MilStdSymbol
         * @param SymbolInfo something like "SymbolID?LineColor=0x000000&FillColor=0xFFFFFF&size=35"
         * @return
         * @author Spinelli
         */
        public static MilStdSymbol createMilstdSymbol(String SymbolInfo)
        {
            String symbolID = null;
            String parameters = null;
            String key = null;
            String value = null;
            String arrParameters[] = null;
            String arrKeyValue[] = null;
            String temp = null;


            Map<String, String> modifiers = new HashMap<String, String>();


            int questionIndex = SymbolInfo.lastIndexOf('?');
            try
            {
                if(questionIndex == -1)
                    symbolID = java.net.URLDecoder.decode(SymbolInfo, "UTF-8");
                else
                    symbolID = java.net.URLDecoder.decode(SymbolInfo.substring(0, questionIndex), "UTF-8");
                //if we're getting good codes, should never get here
                if(symbolID.length() < 15)
                {
                    while(symbolID.length() < 15)
                    {
                        symbolID += "-";
                    }
                }
            }
            catch(Exception exc)
            {
                System.err.println("Error parsing SymbolID");
                System.err.println(exc.getMessage());
            }

            try
            {   //build a map for the other createMilstdSymbol function to use
                //to build a milstd symbol.
                if(questionIndex > 0 && (questionIndex + 1 < SymbolInfo.length()))
                {
                    parameters = SymbolInfo.substring(questionIndex + 1,SymbolInfo.length());
                    arrParameters = parameters.split("&");

                    for(int i = 0; i < arrParameters.length; i++)
                    {
                        arrKeyValue = arrParameters[i].split("=");
                        if(arrKeyValue.length == 2 && arrKeyValue[1]!= null && arrKeyValue[1].equals("")==false)
                        {
                            
                            key = arrKeyValue[0];
                            value = arrKeyValue[1];
                               
                            temp = java.net.URLDecoder.decode(value, "UTF-8");
                            modifiers.put(key, temp);
                            
                            //System.out.println("key: " + key + " value: " + temp);
                        }
                    }
                }
            }
            catch(Exception exc)
            {
                System.err.println("Error parsing \"" + key + "\" parameter from URL");
                System.err.println(exc.getMessage());
            }

            return createMilstdSymbol(symbolID, modifiers);

        }
        
         /**
         * Takes a string and parses information to build a MilStdSymbol
         * @param SymbolID
         * @param params
         * @return
         * @author Spinelli
         */
	public static MilStdSymbol createMilstdSymbol(String symbolID, Map<String,String> params)
        {
            MilStdSymbol symbol = null;
            String key = null;
            String value = null;
            String lineColor = null;
            String fillColor = null;
            String size = null;
            String scale = null;
            String keepUnitRatio = null;
            String alpha = null;
            String symbolOutlineWidth = null;
            String symbolOutlineColor = null;
            String symbologyStandard = null;
            String temp = null;

            //ArrayList<String> tgModifier = ModifiersTG.GetModifierList();
            //ArrayList<String> feModifier = ModifiersUnits.GetModifierList();

            Map<String, String> modifiers = new HashMap<String, String>();

            try
            {
                if(params != null && params.isEmpty()==false)
                {
                    for(Map.Entry<String, String> entry : params.entrySet())
                    {

                        key = entry.getKey();
                        value = entry.getValue();

                        if(key.equalsIgnoreCase(MilStdAttributes.LineColor))
                        {
                            lineColor = value;
                        }
                        else if(key.equalsIgnoreCase(MilStdAttributes.FillColor))
                        {
                            fillColor = value;
                        }
                        else if(key.equalsIgnoreCase(MilStdAttributes.PixelSize))
                        {
                            size = value;
                        }
                        else if(key.equalsIgnoreCase(MilStdAttributes.Scale))
                        {
                            if(SymbolUtilities.isNumber(value))
                            {
                                scale = value;
                            }
                        }
                        else if(key.equalsIgnoreCase(MilStdAttributes.KeepUnitRatio))
                        {
                            keepUnitRatio = value;
                        }
                        else if(key.equalsIgnoreCase(MilStdAttributes.Alpha))
                        {
                            if(SymbolUtilities.isNumber(value))
                            {
                                alpha = value;
                                //System.out.println("parsed alpha is: " + String.valueOf(alpha));
                            }
                        }
                        else if((key.equalsIgnoreCase(MilStdAttributes.OutlineSymbol)))
                        {
                            symbolOutlineWidth = value;
                        }
                        else if((key.equalsIgnoreCase(MilStdAttributes.OutlineColor)))
                        {
                            symbolOutlineColor = value;
                        }
                        else if((key.equalsIgnoreCase(MilStdAttributes.SymbologyStandard)))
                        {
                            symbologyStandard = value;
                        }
//                            else if(key.equalsIgnoreCase("meta"))
//                            {
//                                //ignore meta parameter
//                            }
//                            else if(key.equalsIgnoreCase("renderer"))
//                            {
//                                modifiers.put(key, value.toString());
//                            }
//                            else//assume modifier
//                            {
                            temp = value.toString();
                            //parse out unsafe special characters

                            //<editor-fold defaultstate="collapsed" desc="manual character replacing code">
                            /*
                            temp = temp.replace("%20", " ");
                            temp = temp.replace("%22", "\"");
                            temp = temp.replace("%3C", "<");
                            temp = temp.replace("%3E", ">");
                            temp = temp.replace("%23", "#");
                            temp = temp.replace("%25", "%");
                            //reserved characters
                            temp = temp.replace("%24", "$");
                            temp = temp.replace("%26", "&");
                            temp = temp.replace("%2B", "+");
                            temp = temp.replace("%2C", ",");
                            temp = temp.replace("%2F", "/");
                            temp = temp.replace("%3A", ":");
                            temp = temp.replace("%3B", ";");
                            temp = temp.replace("%3D", "=");
                            temp = temp.replace("%3F", "?");
                            temp = temp.replace("%40", "@");
                            //misc unsafe characters
                            temp = temp.replace("%7B", "{");
                            temp = temp.replace("%7D", "}");
                            temp = temp.replace("%7C", "|");
                            temp = temp.replace("%5C", "\\");
                            temp = temp.replace("%5E", "^");
                            temp = temp.replace("%7E", "~");
                            temp = temp.replace("%5B", "[");
                            temp = temp.replace("%5D", "]");
                            temp = temp.replace("%60", "`");//*/
                            // </editor-fold>

                            //temp = java.net.URLDecoder.decode(temp, "UTF-8");
                            modifiers.put(key, temp);
//                            }

                        
                    }
                }
            }
            catch(Exception exc)
            {
                System.err.println("Error parsing \"" + key + "\" parameter from URL");
                System.err.println(exc.getMessage());
            }

            try
            {
                //BUILD SYMBOL AND SET PROPERTIES
                ArrayList<Point2D.Double> coordinates = new ArrayList<Point2D.Double>();
                coordinates.add(new Point2D.Double(0.0, 0.0));
                //create modifiers
                
                symbol = new MilStdSymbol(symbolID, null, coordinates, modifiers);
                
                //Set Symbology Standard////////////////////////////////////////
                if(symbologyStandard != null)
                {
                    if(symbologyStandard.equalsIgnoreCase("2525B"))
                        symbol.setSymbologyStandard(RendererSettings.Symbology_2525Bch2_USAS_13_14);
                    else
                        symbol.setSymbologyStandard(RendererSettings.Symbology_2525C);
                }
                

                SymbolDef sd = null;
                Boolean isMultiPoint = false;
                if(SymbolUtilities.isTacticalGraphic(symbolID))
                {
                    sd = SymbolDefTable.getInstance().getSymbolDef(SymbolUtilities.getBasicSymbolID(symbolID),symbol.getSymbologyStandard());
                    if(sd != null && sd.getDrawCategory() != SymbolDef.DRAW_CATEGORY_POINT)
                    {
                        if(TacticalGraphicIconRenderer.getInstance().CanRender(symbolID));
                        {
                            isMultiPoint = true;
                        }
                    }
                }
                if(isMultiPoint==false)
                {
                    if(JavaRenderer.getInstance().CanRender(symbolID, null,symbol.getSymbologyStandard())==false)
                    {
                       symbolID = SymbolUtilities.reconcileSymbolID(symbolID,isMultiPoint);
                       symbol.setSymbolID(symbolID);
                       symbol.setLineColor(SymbolUtilities.getLineColorOfAffiliation(symbolID));
                       symbol.setFillColor(SymbolUtilities.getFillColorOfAffiliation(symbolID));
                    }
                }    
                
   
                //set image size in pixels//////////////////////////////////////
                int unitSize;
                if(size != null && SymbolUtilities.isNumber(size))
                {
                    unitSize = Integer.valueOf(size);
                    symbol.setUnitSize(unitSize);
                }
                else if(SymbolUtilities.isTacticalGraphic(symbolID)==false &&
                        SymbolUtilities.isWeather(symbolID)==false)
                {
                    unitSize = 35;
                    symbol.setUnitSize(unitSize);
                }
                
                //set scaling value for single point tactical graphics
                if(scale != null && SymbolUtilities.isNumber(scale))
                {
                    symbol.setScale(Double.parseDouble(scale));
                    //symbol.setUnitSize(0);
                }

                //keep unit size relative to other symbols//////////////////////
                if(keepUnitRatio != null)
                {
                    symbol.setKeepUnitRatio(Boolean.parseBoolean(keepUnitRatio));
                }
                else
                {
                    //will make sure the units keep size relative to each other
                    //assuming google earth doesn't resize them.
                    symbol.setKeepUnitRatio(Boolean.TRUE);
                }

                if(lineColor != null)
                {

                    try
                    {
                        Color lc = SymbolUtilities.getColorFromHexString(lineColor);
                        //System.out.println(String.valueOf(lc.getAlpha()));
                        symbol.setLineColor(lc);
                    }
                    catch(Exception nfe1)
                    {
                        System.err.println("Error parsing lineColor: " + lineColor);
                        System.err.println(nfe1.getMessage());
                    }
                }

                if(fillColor != null)
                {
                    try
                    {
                        Color fc = SymbolUtilities.getColorFromHexString(fillColor);
                        //System.out.println(String.valueOf(fc.getAlpha()));
                        symbol.setFillColor(fc);
                    }
                    catch(Exception nfe2)
                    {
                        System.err.println("Error parsing fillColor: " + fillColor);
                        System.err.println(nfe2.getMessage());
                    }
                }

                if(alpha != null )
                {
                    Color temp1 = symbol.getLineColor();
                    Color temp2 = symbol.getFillColor();
                    if(SymbolUtilities.isNumber(alpha))
                    {

                        int A = Integer.parseInt(alpha);
                        if(A < 0 || A > 255)
                        {
                            A = 255;
                        }

                        symbol.setLineColor(new Color(temp1.getRed(),temp1.getGreen(),temp1.getBlue(),A));
                        symbol.setFillColor(new Color(temp2.getRed(),temp2.getGreen(),temp2.getBlue(),A));
                    }
                }
                
                //outline single point symbols
                
                if(symbolOutlineWidth != null)
                {
                    int width = 0;
                    try
                    {
                        width = Integer.parseInt(symbolOutlineWidth);
                        if(width > 0)
                        {
                            symbol.setOutlineEnabled(true, width);
                            //System.out.println("outline is " + String.valueOf(width));
                        }
                        else
                        {
                            symbol.setOutlineEnabled(false,0);
                            //System.out.println("outline disabled");
                        }
                    }
                    catch(NumberFormatException nfe)
                    {
                        //do nothing
                    }
                }
                
                if(symbol.getOutlineEnabled());
                {
                    if(symbolOutlineColor != null)
                    {
                        symbol.setOutlineColor(SymbolUtilities.getColorFromHexString(symbolOutlineColor));
                        //System.out.println("outline color is " + symbol.getOutlineColor().toString());
                    }
                }
                
            }
            catch(Exception exc)
            {
                System.err.println("Error building MilStdSymbol");
                System.err.println(exc.getMessage());
            }

            return symbol;
        }

        /**
         * Try to turn a bad code into something renderable.
         * @param SymbolID
         * @return
         * @deprecated use SymbolUtilties.reconcileSymbolID() 9/5/2013
         */
        public static String ReconcileSymbolID(String symbolID)
        {
            StringBuilder sb = new StringBuilder("");
            char codingScheme = symbolID.charAt(0);
            
            if(symbolID.length() < 15)
            {
                while (symbolID.length() < 15)
                {
                    symbolID += "-";
                }
            }
            if(symbolID.length() > 15)
            {
                symbolID = symbolID.substring(0, 14);
            }

            if(symbolID != null && symbolID.length()==15)
            {
                if(codingScheme=='S' || //warfighting
                        codingScheme=='I' ||//sigint
                        codingScheme=='O' ||//stability operation
                        codingScheme=='E')//emergency management
                {
                    sb.append(codingScheme);

                    if(SymbolUtilities.hasValidAffiliation(symbolID)==false)
                        sb.append('U');
                    else
                        sb.append(symbolID.charAt(1));

                    if(SymbolUtilities.hasValidBattleDimension(symbolID)==false)
                    {
                        sb.append('Z');
                        sb.replace(0, 1, "S");
                    }
                    else
                        sb.append(symbolID.charAt(2));

                    if(SymbolUtilities.hasValidStatus(symbolID)==false)
                        sb.append('P');
                    else
                        sb.append(symbolID.charAt(3));

                    sb.append("------");
                    sb.append(symbolID.substring(10, 15));

                }
                else if(codingScheme=='G')//tactical
                {
                    sb.append(codingScheme);

                    if(SymbolUtilities.hasValidAffiliation(symbolID)==false)
                        sb.append('U');
                    else
                        sb.append(symbolID.charAt(1));

                    //if(SymbolUtilities.hasValidBattleDimension(SymbolID)==false)
                        sb.append('G');
                    //else
                    //    sb.append(SymbolID.charAt(2));

                    if(SymbolUtilities.hasValidStatus(symbolID)==false)
                        sb.append('P');
                    else
                        sb.append(symbolID.charAt(3));

                    sb.append("GPP---");//return an action point
                    //sb.append("GAG---");//return a boundary
                    sb.append(symbolID.substring(10, 15));


                }
                else if(codingScheme=='W')//weather
                {//no default weather graphic
                    return "SUZP-----------";//unknown
                }
                else//bad codingScheme
                {
                    sb.append('S');
                    if(SymbolUtilities.hasValidAffiliation(symbolID)==false)
                        sb.append('U');
                    else
                        sb.append(symbolID.charAt(1));

                    if(SymbolUtilities.hasValidBattleDimension(symbolID)==false)
                    {
                        sb.append('Z');
                        //sb.replace(0, 1, "S");
                    }
                    else
                        sb.append(symbolID.charAt(2));

                    if(SymbolUtilities.hasValidStatus(symbolID)==false)
                        sb.append('P');
                    else
                        sb.append(symbolID.charAt(3));

                    sb.append("------");
                    sb.append(symbolID.substring(10, 15));
                }
            }
            else
            {
                return "SUZP-----------";//unknown
            }

            return sb.toString();
            
        }
        
    public static boolean is3dSymbol(String symbolCode, String modifiers)
    {
        boolean returnValue = false;
        
        try 
        {
            String symbolId = symbolCode.substring(4, 10);

            if(symbolId.equals("ACAI--") ||
                symbolId.equals("ACAR--") ||
                symbolId.equals("ACAC--") ||
                symbolId.equals("AKPC--") ||
                symbolId.equals("AKPI--") ||
                symbolId.equals("AKPR--") ||
                symbolId.equals("AKPI--") ||
                symbolId.equals("ALC---") ||
                symbolId.equals("ALM---") ||
                symbolId.equals("ALS---") ||
                symbolId.equals("ALU---") ||
                symbolId.equals("ALL---") ||
                symbolId.equals("AAR---") ||
                symbolId.equals("AAF---") ||
                symbolId.equals("AAH---") ||
                symbolId.equals("AAM---") ||
                symbolId.equals("AAF---") ||
                symbolId.equals("AAML--") ||
                symbolId.equals("AAMH--"))
            {                        

                try {
                    if (modifiers != null && modifiers.equals("") == false) {
                        JSONObject jsonModifiersString = new JSONObject(modifiers);
                        JSONObject jsonModifiersArray =
                                jsonModifiersString.getJSONObject("modifiers");

                        // These guys store array values.  Put in appropriate data strucutre
                        // for MilStdSymbol.
                        if (jsonModifiersArray.has("altitudeDepth") && !jsonModifiersArray.isNull("altitudeDepth")) {
                            JSONArray jsonAltitudeArray = jsonModifiersArray.getJSONArray("altitudeDepth");
                            if (jsonAltitudeArray.length() < 2)
                            {
                                returnValue = false;                                    
                            }
                            else 
                            {
                                returnValue = true;
                            }
                        }                    

                    }
                } catch (JSONException je) {
                    //ErrorLogger.LogException(this.getName(), "is3DSymbol()", je);// je.printStackTrace();
                    System.err.println(je.getMessage());
                }                       
            }
        }
        catch(Exception e)
        {
            //ErrorLogger.LogException(this.getName(), "is3DSymbol()", e);// e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return returnValue;
    }
    
    /**
    * Determines if a String represents a valid number
    * @param text
    * @return "1.56" == true, "1ab" == false
    */
    public static boolean isNumber(String text)
    {
       if(text != null && text.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
         return true;
       else
         return false;
    }
    
        /**
     * Takes a throwable and puts it's stacktrace into a string.
     * @param thrown
     * @return
     */
    public static String getStackTrace(Throwable thrown)
    {
        try
        {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            thrown.printStackTrace(printWriter);
            return writer.toString();
        }
        catch(Exception exc)
        {
            //System.out.println("JavaRendererUtilties.getStackTrace()");
            //return "Error - couldn't retrieve stack trace";
            return "";
        }
    }
    
    public static Point2D getEndPointWithAngle(Point2D ptStart,
    //Point2D pt1,
    //Point2D pt2,
    double angle,
    double distance)
    {
        double newX = 0;
        double newY = 0;
        Point2D pt = new Point2D.Double();
        try
        {
            //first get the angle psi between pt0 and pt1
            double psi = 0;//Math.atan((pt1.y - pt0.y) / (pt1.x - pt0.x));
            //double psi = Math.atan((ptStart.getY() - ptStart.getY()) / (ptStart.getX() - (ptStart.getX()+100)));
            //convert alpha to radians
            double alpha1 = Math.PI * angle / 180;

            //theta is the angle of extension from the x axis
            double theta = psi + alpha1;
            //dx is the x extension from pt2
            double dx = distance * Math.cos(theta);
            //dy is the y extension form pt2
            double dy = distance * Math.sin(theta);
            newX = ptStart.getX() + dx;
            newY = ptStart.getY() + dy;
            
            pt.setLocation(newX, newY);
        }
        catch (Exception exc)
        {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
        return pt;
    }
    

    
}
