#### 文字排版对齐解决方案
> 在使用系统的TextView，英文或者中英混排时，文字排版会在一行文字的末尾留下或多或少的空白，这样非常难看，所以就重写的TextView的onDraw()方法，根据效果不同，实现了下面两种方案

##### AlignTextView
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

