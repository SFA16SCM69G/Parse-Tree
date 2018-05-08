/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

//this class implements the grammar class of the second assignment for CS585
public class Grammar
{
    private Vector productions;
    private Vector nonTerminals;
    
    //this reads the grammar in from a file
    public Grammar(String f) throws Exception
    {
        productions = new Vector();
        productions.clear();
        nonTerminals = new Vector();
        nonTerminals.clear();
        //load the file
        BufferedReader br = new BufferedReader(new FileReader(f));
        Production p;
        String str = br.readLine();
        String rule[];
        while(str!=null)
        {
            rule = str.split("\t");
            p = new Production();
            p.probability = (new Float(rule[0])).floatValue();
            p.left = rule[1];
            p.right = rule[2].split(" ");
            p.dot = 0;
            p.start = 0;
            productions.add(p);
            addNonTerminal(rule[1]);
            str = br.readLine();
        }
    }
    
    //checks if we've seen this nonterminal; if we haven't, add it
    private void addNonTerminal(String s)
    {
        for(int i=0;i<nonTerminals.size();i++)
        {
            if(((String)nonTerminals.get(i)).compareTo(s)==0)
                return;
        }
        nonTerminals.add(s);
    }
    
    private final boolean isNonTerminal(String s)
    {
        //return true if it's a non-terminal
        for(int i=0;i<nonTerminals.size();i++)
        {
            if(((String)nonTerminals.get(i)).compareTo(s)==0)
                return true;
        }
        return false;
    }
    
    //this function predicts possible completions of p, and adds them to v
    private final void predict(Vector v, Production p,int pos)
    {
        Vector prods = getProds(p.right[p.dot]);
        Production q,r;
        //        System.out.println("predict:"+p.toString());
        for(int j=0;j<prods.size();j++)
        {
            r = (Production)prods.get(j);
            q = new Production(r);
            q.dot = 0;
            q.start = pos;
            addProd(v,q);
        }
    }
    
    //this checks if we can scan s on the current production p
    private final boolean scan(Vector v, Production p, String s)
    {
        Production q;
        //        System.out.println("scan:"+p.toString());
        if(p.right[p.dot].compareTo(s)==0)
        {
            //match - add it to the next vector
            q = new Production(p);
            q.dot = q.dot + 1;
            addProd(v,q);
            return true;
        }
        return false;
    }
    
    //this takes a completed production and tries to attach it back in the
    //cols table, putting any attachments into cur.
    private final void attach(Vector cols, Vector cur, Production p)
    {
        //if the next thing in one rule is the first thing in this rule,
        //we attach.  otherwise ignore
        Vector col;
        Production q,r;
        String s = p.left;
        boolean match = false;
        
        col = (Vector)cols.get(p.start);
        //        System.out.println("attach:"+p.toString());
        for(int j=0;j<col.size();j++)
        {
            q = (Production)col.get(j);
            if(q.right.length > q.dot)
                if(q.right[q.dot].compareTo(s)==0)
                {    //Attach!
                    r = new Production(q);
                    r.dot = r.dot + 1;
                    addProd(cur,r);
                    //This assignment is added for inducing relationship of Production p and Produciton r
                    r.pointer = p;
                }
        }
    }
    
    //this parses the sentence
    public final Production parse(String sent[])
    {
        //this is a vector of vectors, storing the columns of the table
        Vector cols = new Vector();    cols.clear();
        //this is the current column; a vector of production indices
        Vector cur = new Vector();    cur.clear();
        //this is the next column; a vector of production indices
        Vector next = new Vector();    next.clear();
        //add the first symbol
        cur.add((Production)getProds("ROOT").get(0));
        Production p;
        for(int pos=0;pos<=sent.length;pos++)
        {
            System.out.println("Pos = " + pos);
            int i=0;
            boolean match = false;
            //check through the whole vector, even as it gets bigger
            while(i!=cur.size())
            {
                p = (Production)cur.get(i);
                if(p.right.length > p.dot)
                {    //predict and scan
                    if(sent.length == pos)
                    {
                        match = true;
                    } else{
                        if(isNonTerminal(p.right[p.dot]))
                        {
                            //predict adds productions to cur
                            predict(cur,p,pos);
                        }else{
                            //scan adds productions to next
                            System.out.println("scan: " + p.toString() + " ("+pos+")= " + sent[pos]);
                            if(scan(next,p,sent[pos])) {
                                System.out.println("  Found: " + sent[pos]);
                                match = true;
                            }
                        }
                    }
                } else {    //attach
                    attach(cols,cur,p);
                    if(sent.length == pos)
                    {
                        match = true;
                    }
                }
                i++;
                //when using a gargantuan grammar
                //this spits out stuff if it's taking a long time.
                if(i%100 == 0)
                    System.out.print(".");
            }
            System.out.println("Match = " + match);
            cols.add(cur);
            if(!match)
            {
                printTable(cols,sent);
                System.out.println("Failed on: "+ cur);
                return null;
            }
            cur = next;
            next = new Vector();    next.clear();
            System.out.println();
        }
        
        //print the Earley table
        //Comment this out once you've got parses printing; it's
        //only here for your evaluation.
        printTable(cols,sent);
        //Right now we simply check to see if a parse exists;
        //in other words, we see if there's a "ROOT -> x x x ."
        //production in the last column.  If there is, it's returned; otherwise
        //return null.
        //TODO: Return a full parse.
        cur = (Vector)cols.get(cols.size()-1);
        Production finished = new Production((Production)getProds("ROOT").get(0));
        finished.dot = finished.right.length;
        for(int i=0;i<cur.size();i++)
        {
            p = (Production)cur.get(i);
            if(p.equals(finished))
            {
                p = parseTree(cols,p);
                return p;
            }
            
        }
        return null;
    }
    
    /**this prints the table in a human-readable fashion.
     * format is one column at a time, lists the word in the sentence
     * and then the productions for that column.
     * @param cols The columns of the table
     * @param sent the sentence
     */
    private final void printTable(Vector cols,String sent[])
    {
        Vector col;
        //print one column at a time
        for(int i=0;i<cols.size();i++)
        {
            col = (Vector)cols.get(i);
            //sort the columns by
            if(i>0)
            {
                System.out.println("\nColumn "+i+": "+sent[i-1]+"\n------------------------");
            }else{
                System.out.println("\nColumn "+i+": ROOT\n------------------------");
            }
            
            for(int j=0;j<col.size();j++)
            {
                System.out.println(((Production)col.get(j)).toString());
            }
        }
    }
    
    //this adds a production p to the vector v of production indices
    //it also checks for duplicate indices, and skips those
    private final void addProd(Vector v, Production p)
    {
        //check for duplicates
        for(int i=0;i<v.size();i++)
            if(((Production)v.get(i)).equals(p))
                return;
        v.add(p);
    }
    
    //This runs through the columns and returns all the fully parsed productions
    //i.e. those with little dots at the very end.
    private final Vector getFinalProds(Vector cols)
    {
        Vector cur;
        Vector prods = new Vector();    prods.clear();
        Production p;
        for(int i=0; i<cols.size(); i++)
        {
            cur = (Vector)cols.get(i);
            for(int j=0;j<cur.size();j++)
            {
                p = (Production)cur.get(j);
                if(p.right.length == p.dot)
                {
                    if(p.left.compareTo("ROOT")!=0)
                    {
                        prods.add(p);
                    }
                }
            }
        }
        //convert it to an array for returning
        return prods;
    }
    
    //this returns true if a string is in the grammar, false otherwise
    //it's not exactly "comprehensive"... mostly it'll just see if all
    //the tokens in the sentence are terminals.
    private final boolean inGrammar(String s)
    {
        boolean found=false;
        Production p;
        for(int i=0;i<productions.size();i++)
        {
            p = (Production)productions.get(i);
            for(int j=0;j<p.right.length;j++)
                if(p.right[j].indexOf(s)!=-1)
                    found = true;
            //we can't have a string equal to a non-terminal
            if(p.left.compareTo(s)==0)
            {
                System.out.println("String contains a non-terminal - cannot parse");
                return false;
            }
        }
        return found;
    }
    
    //this returns a vector of productions with a left side matching the
    //argument; happy string comparing.
    private final Vector getProds(String left)
    {
        //we store it in a vector for safekeeping
        Vector prods = new Vector();    prods.clear();
        Production p;
        for(int i=0;i<productions.size();i++)
        {
            p = (Production)productions.get(i);
            if(p.left.compareTo(left)==0)
                prods.add(p);
        }
        //convert it to an array for returning
        return prods;
    }
    
    //this checks if the given string[] has a parse tree with this grammar
    //
    public final boolean canParse(String sent[])
    {
        //check if all symbols are in the grammar
        for(int i=0;i<sent.length;i++)
            if(!inGrammar(sent[i]))
                return false;
        return true;
    }
    
    //this prints out the grammar
    public void print()
    {
        System.out.println(this.toString());
    }
    
    //what does every toString function do?
    public String toString()
    {
        String ret = "";
        for(int i=0;i<productions.size();i++)
            ret = ret + ((Production)productions.get(i)).toString() + "\n";
        return ret;
    }
    //This function is added for producing the parse tree and return the root of tree
    public Production parseTree(Vector cols, Production p){
        //this is the current column; a vector of production indices
        Vector cur = new Vector();    cur.clear();
        //This list is to store productions and their children
        //The children are productions of the parent can induce
        List<Production> list = new ArrayList<>();
        //Add the ROOT production into list
        list.add(p);
        //Traverse the DP table from tail to head
        for(int j=cols.size()-1;j>=0;j--){
            cur = (Vector)cols.get(j);
            for(int k=cur.size()-1;k>=0;k--){
                p = (Production)cur.get(k);
                //Production p has a pointer
                if(p.pointer != null){
                    //Traverse the list
                    for(int l=0;l<list.size();l++){
                        //A production in DP table syntax equals to a production in list
                        if(list.get(l).syntaxEquals(p)){
                            //Allocate space for children of list element
                            if(list.get(l).children == null){
                                int childnum = list.get(l).right.length;
                                list.get(l).children = new Production[childnum];
                            }
                            //Inducing relationship is based on the last inducing pointer
                            //Avoid children to be updated
                            if(list.get(l).children[p.dot-1]==null){
                                p.pointer = list.get(l).spawn(p.dot-1, p.pointer);
                            }
                            //This flag is to decide if a production can be added into list
                            int flag = 0;
                            //If there is a non-ternimal symbol at the right of production, set the flag to 1
                            for(int m=0;m<p.pointer.right.length;m++){
                                if(isNonTerminal(p.pointer.right[m])){
                                    flag = 1;
                                }
                            }
                            //If an element in list syntax equals to the production, set the flag to 0
                            for(int m=0;m<list.size();m++){
                                if(list.get(m).syntaxEquals(p.pointer)){
                                    flag = 0;
                                }
                            }
                            //If flag is 1, add the production into list
                            if(flag == 1)
                                list.add(p.pointer);
                        }
                    }
                }
            }
        }
        
        //Initialize the root of parse tree
        Production root = new Production((Production)getProds("ROOT").get(0));
        root.dot = root.right.length;
        //Build the parse tree based on the list
        while(!list.isEmpty()){
            for(int j=0;j<list.size();j++){
                p = list.get(j);
                //If the right of production is non terminal,pass this production
                if(!isNonTerminal(p.pointer.right[0])){
                    list.remove(p);
                    j--;
                    continue;
                }
                //Search the current tree by BFS
                Queue<Production> queue = new ArrayDeque<>();
                Set<Production> set = new HashSet<>();
                queue.add(root);
                while(!queue.isEmpty()){
                    Production q = queue.remove();
                    //A production in the list syntax equals to a production in the parse tree
                    if(q.syntaxEquals(p)){
                        int childnum = q.right.length;
                        q.children = new Production[childnum];
                        //Set the children of production in the list to the children of production in the parse tree
                        for(int k=0;k<p.children.length;k++){
                            p.children[k] = q.spawn(k,p.children[k]);
                        }
                        //Remove production p from the list
                        list.remove(p);
                        j--;
                        break;
                    }
                    if(q.children != null){
                        for(int k=0;k<q.children.length;k++){
                            if(set.contains(q.children[k])){
                                continue;
                            }
                            if(!queue.contains(q.children[k])){
                                queue.add(q.children[k]);
                            }
                        }
                    }
                    //Production is not found in the parse tree
                    if(queue.isEmpty()){
                        list.remove(p);
                        j--;
                    }
                    set.add(q);
                }
            }
        }
        return root;
    }
}
