# JAVLO 2 : HTML/CSS Framework
## Preview
### Collapse
minimal html 
```
<div class="_jv_collapse-container">
	<button class="btn-toggle" data-jv-target="#container" data-jv-toggle="collapse">open</button>
	<div id="container" class="_jv_collapse-target">LoremIpsun</div>
</div>
```
### Menu
class '_jv_menu' wrap list of items
item html
```
<div class="_jv_menu">
	<button type="button" class="btn btn-default">
		<span class="button-group-addon">[icon]</span> 
		<span class="label">[label]</span>
	</button>
	<button type="button" class="btn btn-default">
		<span class="button-group-addon">[icon]</span> 
		<span class="label">[label]</span>
	</button>
</div>
```
### Layout
#### ._jv_full_height_bottom
full height from top of item to bottom of page.
#### _jv_btn-check
Exemple code for check box button
```
<div class="_jv_btn-check">
	<input type="checkbox" name="user" value="me">
	<label for="user">pvandermaesen</label>
</div>
```
##### checkbox
Exemple code for radio group button
```
<div class="btn-group">
	<div class="_jv_btn-check">
		<input id="i1" type="radio" name="i1" value="1">
		<label or="i1">none</label>
	</div>
	<div class="_jv_btn-check">
		<input id="i2" type="radio" name="i2" value="2">
		<label or="i2">none</label>
	</div>
</div>
```
## Edit
### Layout
#### _jv_collapsable fieldset

	add class collapsable on a `<fieldset>`, I will web collapsable with click on legend.