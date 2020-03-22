#### 文字排版对齐解决方案
> 在使用系统的TextView，英文或者中英混排时，文字排版会在一行文字的末尾留下或多或少的空白，这样非常难看，所以就重写的TextView的onDraw()方法，根据效果不同，实现了下面两种方案

##### AlignTextView  纯英文排版对齐
> 原理：利用StaticLayout测量出要绘制的这一行文字的宽度，然后利用ViewGroup分配的总宽度，计算出每个字符之间的间距，然后再依次绘制每个字符
> 优点：避免出现每一行文字末尾的较长宽度的空白
> 缺点：可能有的时候字符之间的间距会增大
>
>  ```xml
> <com.hsicen.aligntextview.AlignTextView
>    android:id="@+id/align_text"
>    android:layout_width="0dp"
>    android:layout_height="wrap_content"
>    android:layout_margin="10dp"
>    android:textColor="@android:color/holo_purple"
>    android:textSize="18sp"
>    app:layout_constraintLeft_toLeftOf="parent"
>    app:layout_constraintRight_toRightOf="parent"
 >    app:layout_constraintTop_toTopOf="parent" />
>```


##### MixAlignTextView 文字两端对齐，以及文字截断
> 原理：先将文字按照段为单位进行拆分，然后再将没一段按照汉字或者单词为最小单位进行拆分，将得到的按段为单位的结合进行遍历，将每一段以行为单位进行拆分，这里为关键点，断行的逻辑都在这里处理，将最后的到的以段为大单位，行为中单位，汉字或者单词为最小单位的数据进行绘制
>
> ``` xml
>  <com.hsicen.aligntextview.MixAlignTextView
>       android:id="@+id/mix_align_text"
>       android:layout_width="0dp"
>       android:layout_height="wrap_content"
>       android:layout_margin="10dp"
>       android:textColor="@android:color/holo_green_dark"
>       android:textSize="18sp"
>       app:layout_constraintLeft_toLeftOf="parent"
>       app:layout_constraintRight_toRightOf="parent"
>       app:layout_constraintTop_toTopOf="parent" />
> ```
