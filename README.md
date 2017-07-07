# ImageEditContainer
本次demo中一共封装了两个组件：ImageEditButton 和 ImageEditContainer。其中ImageEditContainer 是在 ImageEditButton，两个组件可单独使用。

在demo中，实现了 图片选择（拍照+本地），裁剪，压缩，保存本地 以及对已选择图片的删除操作（如果有修改需求，也可以使用对应方法进行操作，该方法已添加）；

 还有就是 针对 6.0权限的处理问题，本次使用了第三方库 rxpermissions 进行权限的处理。
