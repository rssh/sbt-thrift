namespace *   atd.sbtthrift
namespace php grad


typedef string decimal

exception AuthorizationException
{
 1: required string message;
 2: required i16    code;
}



service TestThrift
{

  string testOp(1: required string x1,
                2: required string x2,
                3: required decimal x3,
                4: required i64 x4)
                           throws(1: AuthorizationException ex)


}


