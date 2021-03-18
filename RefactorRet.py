import os.path
import codecs


def _file_traverse(filepath, des):
    path_dir = os.listdir(filepath)
    for dirs in path_dir:
        child = os.path.join('%s/%s' % (filepath, dirs))
        if os.path.isfile(child):
            print(child)
            if not _is_binary_file(child):
                if _read_file(child):
                    return True
            continue
        _file_traverse(child)
    return False


def _read_file(filename):
    fop = open(filename, 'r')  # 读取文件
    fired = fop.read()  # 获取文件内容
    fop.close()
    for lis in lister:
        if lis in fired:
            print('字符串：%s,文件路径:%s' % (lis, filename))
            return True
    return False


_TEXT_BOM = (
    codecs.BOM_UTF16_BE,
    codecs.BOM_UTF16_LE,
    codecs.BOM_UTF32_BE,
    codecs.BOM_UTF32_LE,
    codecs.BOM_UTF8,
)


def _is_binary_file(file_path):
    with open(file_path, 'rb') as file:
        initial_bytes = file.read(8192)
        file.close()
        for bom in _TEXT_BOM:
            if initial_bytes.startswith(bom):
                continue
            else:
                if b'\0' in initial_bytes:
                    return True
    return False


def need_refactor(des_str):
    filenames = '/home/lijf/code/XPlugin/mylibrary/src/main'
    return _file_traverse(filenames, des_str)
