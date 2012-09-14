<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<st:ifParam parameter="back">
<% response.sendRedirect("createType.jsp?checked=custom"); %>
</st:ifParam>
<st:ifParam parameter="next">
<st:ifParam parameter="variety" value="atomic">
<% response.sendRedirect("createAtomicType.jsp"); %>
</st:ifParam>
<st:ifParam parameter="variety" value="list">
<% response.sendRedirect("createListType.jsp"); %>
</st:ifParam>
<st:ifParam parameter="variety" value="union">
<% response.sendRedirect("createUnionType.jsp"); %>
</st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type Creation">
<form action="createCustomType.jsp" method="post">
<h3>Custom Type Creation</h3>
<p>Custom Types are the most flexible and advanced means to specify the exact set of values that
 a type can have. The process of creating a new Custom Type begins by choosing amoung the three 
 basic varieties of types that you can create: Atomic, List or Union.</p>
<p>Pick a variety that suits the purpose of your new type:</p>
<table border="1" cellpadding="5" cellspacing="1">
<tr><td><input <st:ifParam parameter="customChecked" value="atomic">checked </st:ifParam>type="radio" name="variety" value="atomic" checked></td>
<td class="varietyName">Atomic</td>
<td>An atomic type is a type whose values are considered indivisible. For example, if you have
an atomic type based on a decimal number and a user submits a value of <b>23.06</b>, the individual
digits of <b>2</b>, <b>3</b>, <b>0</b> and <b>6</b> are meaningless by themselves and are only 
considered as a valid value when they are considered all together as <b>23.06</b>. An atomic 
type is always derived from an existing built-in or user-defined type.</p>
<p>Use this option to specify constraints such as maximum/minium length or values, patterns or 
number of digits.</p></td></tr>
<tr><td><input <st:ifParam parameter="customChecked" value="list">checked </st:ifParam>type="radio" name="variety" value="list"></td>
<td class="varietyName">List</td>
<td>A list type is a sequence of atomic types. Each item in a list has to be of the same type
and must conform to the restrictions and values allowed for that type. For example, a list of
type date can be a sequence of dates separated by spaces such as <b>2001-11-10 2001-11-13</b>.
Defining a list type involves specifying the type of it's members.</td></tr>
<tr><td><input <st:ifParam parameter="customChecked" value="union">checked </st:ifParam>type="radio" name="variety" value="union"></td>
<td class="varietyName">Union</td>
<td>A union type is the combination of the values from one or more types. All the values of each
type that is a member of the union are considered valid. This is useful for combining two types 
that you have previously created. 
For example, an Internet address (an URL) and the value "None". The user then has the choice
of entering an URL or the word "None".</td></tr>
</table>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
