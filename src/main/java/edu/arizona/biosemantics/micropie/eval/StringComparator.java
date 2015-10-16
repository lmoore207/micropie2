package edu.arizona.biosemantics.micropie.eval;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;


/**
 * compare the value by exact string matching
 * @author maojin
 *
 */
public class StringComparator implements IValueComparator{

	@Override
	public double compare(List<CharacterValue> extValues,List<CharacterValue> gstValues) {
		if((extValues == null||extValues.size()==0)&&(gstValues==null||gstValues.size()==0)) return 1;
		if(extValues == null||gstValues==null||extValues.size()==0||gstValues.size()==0) return 0;
		//System.out.println(" gst:["+gstValues+"] tg:["+extValues+"]"); 
		int match = 0;
		for(int i=0;i<extValues.size();i++){
			CharacterValue extValue = extValues.get(i);
			String extValueStr = cleanValue(extValue.getValue()).trim();
			if(extValueStr.indexOf("and ")>-1){
				String[] extDiValus = extValueStr.split("and");
				extValueStr = extDiValus[0].trim();
				
				//
				for(int j=1;j<extDiValus.length;j++){
					extValues.add(CharacterValueFactory.create(null, extDiValus[j].trim()));
				}
			}
			Iterator<CharacterValue> gstValueIter = gstValues.iterator();
			while(gstValueIter.hasNext()){
				CharacterValue gstValue = gstValueIter.next();
				//System.out.println(extValueStr+" "+gstValue.getValue()+" "+match);
				String gstValueStr = cleanValue(gstValue.getValue()).trim();
				extValueStr = extValueStr.trim();
				//exactly string match
				//System.out.println(extValueStr+" "+gstValueStr+" "+match);
				//if(extValueStr.equalsIgnoreCase(gstValueStr)&&!"".equals(extValueStr)){
				if(extValueStr.equalsIgnoreCase(gstValueStr)){
					//System.out.println(extValueStr+" "+gstValueStr+" hit "+match);
					match++;
					//gstValues.remove(gstValue);
					break;
				}
			}
		}
		return match;
	}
	
	
	public String cleanValue(String value){
		value = value.replace("-", " ");
		
		//unit
		value = value.replace("mol%", "");
		value = value.replace("·", ".");
		value = value.replace("°C", "");
		value = value.replace("%", " ");
		value = value.replace(" g", " ");
		value = value.replace("sea salts", " ");
		value = value.replace("(w/v)", " ");
		value = value.replace(" NaCl", " ");
		value = value.replace(" M", " ");
		value = value.replace("??????C", "");
		value = value.replace("??C", "");
		value = value.replace("??m", "");
		
		return value;
	}
}