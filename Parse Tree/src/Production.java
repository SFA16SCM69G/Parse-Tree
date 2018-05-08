/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public class Production
{
    float probability;
    String left;
    String right[];
    int dot;
    int start;
    
    //Production does double duty as a parse tree; this is for that.
    //therefore, has same number of children as right[], one for
    //each; if there is no child there, null is stored instead.
    Production children[];
    //parent is for the linking as well.
    Production parent;
    //pointer to a linked production
    Production pointer;
    /**Simple constructor, assumes no children, initializes everybody.*/
    Production()
    {
        probability=0.0f;
        left = "";
        right = null;
        dot = 0;
        start = 0;
        children = null;
        parent = null;
        pointer = null;
    }
    
    /**Constructs a production with n right productions.*/
    Production(int n)
    {
        this();
        right = new String[n];
        children = new Production[n];
        for(int i=0;i<n;i++)
        {
            right[i] = null;
            children[i] = null;
        }
    }
    
    /**Constructs a production with the given right hand side.*/
    Production(String[] rhs)
    {
        this(rhs.length);
        right = rhs;
    }
    
    /**Copy constructor.*/
    Production(Production p)
    {
        probability = p.probability;
        left = p.left;
        right = p.right;
        dot = p.dot;
        start = p.start;
        children = p.children;
        pointer = p.pointer;
    }
    
    /**This creates a child of the production given its index.
     * This adds the child to the production and sets the parent for
     * the newly created child production.
     *
     * @param n the index on the right hand side where the child attaches
     * @return The newly created child
     */
    public final Production spawn(int n)
    {
        Production p = new Production();
        p.parent = this;
        children[n] = p;
        return p;
    }
    
    /**This creates a child of the production given its index.
     * This adds the child to the production and sets the parent for
     * the newly created child production.  The new child production
     * will be a copy of the production input as a parameter.
     *
     * @param n the index on the right hand side where the child attaches
     * @param prod the production to copy the child from
     * @return The newly created child
     */
    public final Production spawn(int n, Production prod)
    {
        Production p = new Production(prod);
        p.parent = this;
        children[n] = p;
        return p;
    }
    
    /**This returns true if the given production matches this one.
     *
     * The comparison checks for identical productions only, down to the
     * placement of the dot.
     *
     * @param p The production to compare to.
     */
    public final boolean equals(Production p)
    {
        if(left != p.left || right.length != p.right.length || dot != p.dot || start != p.start)
            return false;
        for(int i=0;i<right.length;i++)
            if(right[i] != p.right[i])
                return false;
        return true;
    }
    
    public final boolean syntaxEquals(Production p)
    {
        if(left != p.left || right.length != p.right.length)
            return false;
        for(int i=0;i<right.length;i++)
            if(right[i] != p.right[i])
                return false;
        return true;
    }
    
    /**Easy print.
     */
    public void print()
    {
        System.out.println(this.toString());
    }
    
    /**Standard toString human-readable output.
     * Format:
     * startpos  left-- right1 . right2
     * with the dot moving about accordingly.
     */
    public String toString()
    {
        String ret = start+"\t"+left+"->";
        for(int i=0;i<right.length;i++)
        {
            if(i==dot)
                ret = ret + "\t.";
            ret = ret + "\t" + right[i];
        }
        if(dot == right.length)
            ret = ret + "\t.";
        return ret;
    }
    
    /**This prints a parse, a chain of productions.
     * TODO: Write this function!
     */
    //Print the parse tree by recursive DFS traversal
    public void recursivePrint()
    {
        if(this == null)
            return;
        System.out.print(left+"[");
        if(children == null){
            System.out.print(right[0]);
        }
        if(children != null){
            for(int i=0;i<children.length;i++){
                children[i].recursivePrint();
            }
        }
        System.out.print("]");
    }
    //Compute the probability and print the result
    public void computeProb(){
        //Initialize the probability
        float result = 1.0f;
        Production p = new Production();
        //Traverse the tree by BFS
        Queue<Production> queue = new ArrayDeque<>();
        Set<Production> set = new HashSet<>();
        queue.add(this);
        while(!queue.isEmpty()){
            p = queue.remove();
            result = result * p.probability;
            if(p.children != null){
                for(int i=0;i<p.children.length;i++){
                    if(set.contains(p.children[i])){
                        continue;
                    }
                    if(!queue.contains(p.children[i])){
                        queue.add(p.children[i]);
                    }
                }
            }
        }
        //Print result
        System.out.println("The probability of this sentence is "+result);
    }
}
